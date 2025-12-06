package com.readingtracker.agent;

import static net.bytebuddy.matcher.ElementMatchers.named;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.stream.Collectors;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Runtime ByteBuddy agent that injects an all-args constructor into classes
 * annotated with `@CacheEntity`.
 *
 * This implementation adds a public constructor with parameters matching
 * the non-static declared fields of the type and sets fields from arguments.
 */
public class CacheEntityAgent {

    private static final String CACHE_ENTITY_ANNOTATION = "com.sharedsync.shared.annotation.CacheEntity";

    public static void installAgent() {
        try {
            // Ensure the runtime agent is installed
            Instrumentation inst = ByteBuddyAgent.install();

            new AgentBuilder.Default()
                    .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                    .type(ElementMatchers.isAnnotatedWith(named(CACHE_ENTITY_ANNOTATION)))
                    .transform((builder, typeDescription, classLoader, module, protectionDomain) -> {
                        // collect non-static fields
                        List<FieldDescription.InDefinedShape> fields = typeDescription.getDeclaredFields()
                                .filter(fd -> !fd.isStatic())
                                .stream()
                                .map(f -> (FieldDescription.InDefinedShape) f)
                                .collect(Collectors.toList());

                        // build parameter types
                        TypeDescription[] paramTypes = fields.stream()
                                .map(f -> f.getType().asErasure())
                                .toArray(TypeDescription[]::new);

                        // Build a Composable implementation: SuperMethodCall followed by field setters
                        net.bytebuddy.implementation.Implementation.Composable impl = SuperMethodCall.INSTANCE;
                        for (int i = 0; i < fields.size(); i++) {
                            String fieldName = fields.get(i).getName();
                            impl = impl.andThen(FieldAccessor.ofField(fieldName).setsArgumentAt(i));
                        }

                        return builder.defineConstructor(Visibility.PUBLIC)
                                .withParameters(paramTypes)
                                .intercept(impl);
                    })
                    .installOn(inst);

            System.out.println("[CacheEntityAgent] installed ByteBuddy transformer for @CacheEntity");
        } catch (Throwable t) {
            t.printStackTrace();
            System.err.println("[CacheEntityAgent] failed to install agent: " + t.getMessage());
        }
    }
}
