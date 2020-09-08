package org.powerapi.jjoules.spoon;

import org.powerapi.jjoules.junit.EnergyTest;
import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * 07/09/2020
 */
public class AnnotationsReplacer extends AbstractProcessor<CtClass<?>> {

    @Override
    public boolean isToBeProcessed(CtClass<?> candidate) {
        return super.isToBeProcessed(candidate);
    }

    @Override
    public void process(CtClass<?> ctClass) {
        final Factory factory = ctClass.getFactory();
        final CtAnnotation<Annotation> annotation =
                factory.createAnnotation(factory.createCtTypeReference(EnergyTest.class));
        final CtTypeReference junit5AnnotationTest = factory.createReference("org.junit.jupiter.api.Test");
        final Optional<CtMethod<?>> any = ctClass.getMethodsAnnotatedWith(junit5AnnotationTest)
                .stream()
                .findAny();
        any.ifPresent(ctMethod -> ctMethod
                .getAnnotation(junit5AnnotationTest)
                .replace(annotation));
    }
}
