package org.powerapi.jjoules.spoon;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.powerapi.jjoules.junit.EnergyTest;
import spoon.Launcher;
import spoon.OutputType;
import spoon.SpoonModelBuilder;
import spoon.compiler.Environment;
import spoon.reflect.declaration.*;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.JavaOutputProcessor;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * 07/09/2020
 */
@Mojo(name = "replace-annotation", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE)
public class AnnotationsReplacerMojo extends AbstractMojo {

    public void process(CtType<?> ctClass) {
        final Factory factory = ctClass.getFactory();
        final CtAnnotation<Annotation> annotation =
                factory.createAnnotation(factory.createCtTypeReference(EnergyTest.class));
        final CtTypeReference junit5AnnotationTest = factory.createReference("org.junit.jupiter.api.Test");
        final Set<CtMethod<?>> methodsAnnotatedWith = ctClass.getMethods();//getMethodsAnnotatedWith(junit5AnnotationTest);
        methodsAnnotatedWith
                .forEach(method -> {
                            System.out.printf("%s#%s%n", ctClass.getQualifiedName(), method.getSimpleName());
                            System.out.println(method.getAnnotations());
                            if (method.getAnnotation(junit5AnnotationTest) != null) {
                                method.getAnnotation(junit5AnnotationTest).replace(annotation);
                            }
                            method.setSimpleName(method.getSimpleName() + "_spooned0");
                            System.out.printf("%s#%s%n", ctClass.getQualifiedName(), method.getSimpleName());
                            System.out.println(method.getAnnotations());
                        }
                );
        Environment env = factory.getEnvironment();
        try {
            env.setAutoImports(false);
            env.setNoClasspath(true);
            env.setCommentEnabled(true);
            JavaOutputProcessor processor = new JavaOutputProcessor(env.createPrettyPrinter());
            processor.setFactory(factory);
            processor.getEnvironment().setSourceOutputDirectory(new File("target/generated-test-sources/"));
            processor.createJavaFile(ctClass);
            env.setAutoImports(false);
        } catch (Exception ignored) {

        }
    }

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Launcher launcher = new Launcher();
//        launcher.addInputResource(mavenProject.getCompileSourceRoots().get(0).toString());
        launcher.addInputResource(mavenProject.getTestCompileSourceRoots().get(0).toString());
        launcher.buildModel();
        launcher.getFactory().Class().getAll().forEach(this::process);
    }
}
