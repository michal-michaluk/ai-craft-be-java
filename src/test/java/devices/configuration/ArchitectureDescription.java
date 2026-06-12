package devices.configuration;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;

import java.util.Arrays;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.base.DescribedPredicate.or;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.*;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchitectureDescription {

    public static final DescribedPredicate<JavaClass> adapters = or(
            simpleNameEndingWith("Controller"),
            simpleNameEndingWith("Repository"),
            simpleNameEndingWith("Projection"),
            simpleNameEndingWith("Entity"),
            simpleNameEndingWith("Integration"),
            simpleNameEndingWith("Listener"),
            simpleNameEndingWith("Client")
    );

    public static final DescribedPredicate<JavaClass> services = simpleNameEndingWith("Service");

    public static final DescribedPredicate<JavaClass> model = not(services.or(adapters));

    public static final DescribedPredicate<JavaClass> mediators = resideInAPackage(
            "..mediators.."
    );

    public static ClassesShouldConjunction adaptersDependencies(String aPackage, DescribedPredicate<JavaClass> sharedKernelUsed) {
        return classes()
                .that(adapters).and().resideInAPackage(aPackage)
                .should()
                .onlyDependOnClassesThat(or(
                        sharedKernelUsed,
                        resideInAnyPackage(aPackage),
                        resideInAnyPackage("devices.configuration.tools.."),
                        resideOutsideOfPackage(parentOf(aPackage) + "..")
                ));
    }

    public static ClassesShouldConjunction servicesDependencies(String aPackage, DescribedPredicate<JavaClass> sharedKernelUsed) {
        return classes()
                .that(services).and().resideInAPackage(aPackage)
                .should()
                .onlyDependOnClassesThat(or(
                        sharedKernelUsed,
                        resideInAnyPackage(aPackage, "java..", "lombok.."),
                        resideInAnyPackage("org.springframework.stereotype",
                                "org.springframework.transaction.annotation",
                                "org.springframework.context..")
                ));
    }

    public static ClassesShouldConjunction modelDependencies(String aPackage, DescribedPredicate<JavaClass> sharedKernelUsed) {
        return classes()
                .that(model).and().resideInAPackage(aPackage)
                .and().areTopLevelClasses()
                .should()
                .onlyDependOnClassesThat(or(
                        model,
                        sharedKernelUsed,
                        resideInAnyPackage("java..", "lombok.."),
                        resideInAnyPackage("javax.validation.."),
                        resideInAnyPackage("com.fasterxml.jackson..")
                ));
    }

    public static ClassesShouldConjunction adaptersIsolation(String aPackage) {
        return classes()
                .that(adapters).and().resideInAPackage(aPackage)
                .should()
                .onlyBeAccessed().byClassesThat()
                .resideInAPackage(aPackage);
    }

    public static ClassesShouldConjunction servicesIsolation(String aPackage, DescribedPredicate<JavaClass> isAllowedUsers) {
        return classes()
                .that(services).and().resideInAPackage(aPackage)
                .should()
                .onlyBeAccessed().byClassesThat(
                        resideInAnyPackage(aPackage, aPackage + "..").or(mediators).or(isAllowedUsers)
                );
    }

    public static ClassesShouldConjunction modelIsolation(String aPackage, DescribedPredicate<JavaClass> sharedKernelExposed) {
        return classes()
                .that(model).and().resideInAPackage(aPackage)
                .and(not(sharedKernelExposed))
                .should()
                .onlyBeAccessed().byClassesThat(
                        resideInAnyPackage(aPackage, aPackage + "..")
                );
    }

    private static String parentOf(String aPackage) {
        String[] packages = aPackage.split("\\.");
        return String.join(".", Arrays.copyOf(packages, packages.length - 1));
    }
}
