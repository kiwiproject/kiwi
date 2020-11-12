package org.kiwiproject.spring.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.kiwiproject.base.KiwiStrings.f;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kiwiproject.spring.config.sample1.SampleJavaConfigClass1;
import org.kiwiproject.spring.config.sample2.SampleJavaConfigClass2;

@DisplayName("KiwiSpringJavaConfigs")
class KiwiSpringJavaConfigsTest {

    @Nested
    class PackagesToScanForEntities {

        @Test
        void shouldThrowIllegalArgumentException_WhenGivenNullArray() {
            assertThatThrownBy(() -> KiwiSpringJavaConfigs.packagesToScanForEntities((Class<?>[]) null))
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Null varargs array argument is not allowed");
        }

        @Test
        void shouldThrowIllegalArgumentException_WhenNoClassesSpecified() {
            assertThatThrownBy(KiwiSpringJavaConfigs::packagesToScanForEntities)
                    .isExactlyInstanceOf(IllegalArgumentException.class)
                    .hasMessage("At least one entity class must be specified");
        }

        @Test
        void shouldReturnOnlyPackageName_WhenSingleClassSpecified() {
            var aClass = SampleJavaConfigClass1.class;
            var packagesToScan = KiwiSpringJavaConfigs.packagesToScanForEntities(aClass);
            assertThat(packagesToScan).isEqualTo(aClass.getPackage().getName());
        }

        @Test
        void shouldReturnOnlyPackageName_WhenClassesInSamePackageSpecified() {
            var class1 = JavaConfigClass1.class;
            var class2 = JavaConfigClass2.class;
            var packagesToScan = KiwiSpringJavaConfigs.packagesToScanForEntities(class1, class2);
            assertThat(packagesToScan).isEqualTo(class1.getPackage().getName());
        }

        @Test
        void shouldReturnCommaSeparatedPackageNames_WhenClassesInDifferentPackagesSpecified() {
            var class1 = JavaConfigClass1.class;
            var class2 = JavaConfigClass2.class;
            var class3 = SampleJavaConfigClass1.class;

            var packagesToScan = KiwiSpringJavaConfigs.packagesToScanForEntities(class1, class2, class3);

            var expectedPackages = f("{},{}", class1.getPackage().getName(), class3.getPackage().getName());
            assertThat(packagesToScan).isEqualTo(expectedPackages);
        }

        @Test
        void shouldReturnPackageNamesInNaturalOrder() {
            var class1 = SampleJavaConfigClass2.class;
            var class2 = SampleJavaConfigClass1.class;
            var class3 = JavaConfigClass1.class;
            var class4 = JavaConfigClass2.class;

            var packagesToScan = KiwiSpringJavaConfigs.packagesToScanForEntities(class1, class2, class3, class4);

            var expectedPackages = f("{},{},{}",
                    class3.getPackage().getName(),
                    class2.getPackage().getName(),
                    class1.getPackage().getName());
            assertThat(packagesToScan).isEqualTo(expectedPackages);
        }
    }
}
