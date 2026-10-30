package pe.com.agridron.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pe.com.agridron.shared.domain.events.IntegrationEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Architecture rules for the modular monolith.
 */
@AnalyzeClasses(packages = "pe.com.agridron", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule shared_must_not_depend_on_bounded_contexts =
            noClasses()
                    .that().resideInAPackage("pe.com.agridron.shared..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "pe.com.agridron.iam..",
                            "pe.com.agridron.field..",
                            "pe.com.agridron.flight..",
                            "pe.com.agridron.weather..",
                            "pe.com.agridron.analytics..",
                            "pe.com.agridron.inventory..")
                    .because("shared must stay independent from every bounded context");

    @ArchTest
    static final ArchRule domain_must_not_depend_on_outer_layers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..application..",
                            "..infrastructure..",
                            "..interfaces..")
                    .because("the domain layer must remain free of application, infrastructure and interface concerns");

    @ArchTest
    static final ArchRule application_must_not_depend_on_infrastructure_or_interfaces =
            noClasses()
                    .that().resideInAPackage("..application..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "..infrastructure..",
                            "..interfaces..")
                    .because("application services depend on domain ports, not on adapters");

    @ArchTest
    static final ArchRule integration_events_must_implement_the_contract =
            classes()
                    .that().resideInAPackage("..interfaces.events..")
                    .should().implement(IntegrationEvent.class)
                    .because("every class in interfaces.events is part of the published language between bounded contexts");

    @ArchTest
    static final ArchRule integration_event_handlers_must_live_in_interfaces =
            classes()
                    .that().haveSimpleNameEndingWith("IntegrationEventsHandler")
                    .should().resideInAPackage("..interfaces.eventhandlers..")
                    .because("domain-to-integration translation is an interface adapter");
}
