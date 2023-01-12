package org.kiwiproject.reflect;

import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.kiwiproject.base.KiwiPreconditions.checkArgumentNotNull;

import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.kiwiproject.junit.jupiter.WhiteBoxTest;
import org.kiwiproject.reflect.KiwiReflection.Accessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@DisplayName("KiwiReflection")
@ExtendWith(SoftAssertionsExtension.class)
class KiwiReflectionTest {

    @Nested
    class FindField {

        private Person bob;

        @BeforeEach
        void setUp() {
            bob = new Person("Bob", "Sackamano", 34);
        }

        @Test
        void shouldFindField_WhenExists() {
            var lastNameField = KiwiReflection.findField(bob, "lastName");

            assertThat(lastNameField).isNotNull();
            assertThat(lastNameField.getName()).isEqualTo("lastName");
            assertThat(lastNameField.getType()).isEqualTo(String.class);
        }

        @Test
        void shouldThrowException_WhenDoesNotExist() {
            assertThatThrownBy(() -> KiwiReflection.findField(bob, "fooBar"))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Cannot get field [fooBar] in object [%s]", bob)
                    .hasCauseExactlyInstanceOf(NoSuchFieldException.class);
        }
    }

    @Nested
    class GetTypedFieldValue {

        private Person jerry;

        @BeforeEach
        void setUp() {
            jerry = new Person("Jerry", "Seinfeld", 42);
        }

        @Test
        void shouldAcceptFieldName(SoftAssertions softly) {
            var firstName = KiwiReflection.getTypedFieldValue(jerry, "firstName", String.class);
            softly.assertThat(firstName).isEqualTo("Jerry");

            var age = KiwiReflection.getTypedFieldValue(jerry, "age", Integer.class);
            softly.assertThat(age).isEqualTo(42);
        }

        @Test
        void shouldAcceptFieldObject(SoftAssertions softly) throws NoSuchFieldException {
            var lastNameField = jerry.getClass().getDeclaredField("lastName");
            assertThat(lastNameField.trySetAccessible()).isTrue();
            var lastName = KiwiReflection.getTypedFieldValue(jerry, lastNameField, String.class);
            softly.assertThat(lastName).isEqualTo("Seinfeld");

            var ageField = jerry.getClass().getDeclaredField("age");
            assertThat(ageField.trySetAccessible()).isTrue();
            var age = KiwiReflection.getTypedFieldValue(jerry, ageField, Integer.class);
            softly.assertThat(age).isEqualTo(42);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenFieldDoesNotExist() {
            assertThatThrownBy(() -> KiwiReflection.getTypedFieldValue(jerry, "middleName", String.class))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Cannot get field [middleName] in object [%s]", jerry)
                    .hasCauseExactlyInstanceOf(NoSuchFieldException.class);
        }

        @Test
        void shouldThrowClassCastException_WhenGivenInvalidReturnType() {
            assertThatThrownBy(() -> KiwiReflection.getTypedFieldValue(jerry, "age", int.class))
                    .isExactlyInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    class GetFieldValue {

        private Person jerry;

        @BeforeEach
        void setUp() {
            jerry = new Person("Jerry", "Seinfeld", 42);
        }

        @Test
        void shouldAcceptFieldName() {
            var firstName = (String) KiwiReflection.getFieldValue(jerry, "firstName");
            assertThat(firstName).isEqualTo("Jerry");
        }

        @Test
        void shouldAcceptFieldObject() throws NoSuchFieldException {
            var ageField = jerry.getClass().getDeclaredField("age");
            assertThat(ageField.trySetAccessible()).isTrue();

            var age = (Integer) KiwiReflection.getFieldValue(jerry, "age");
            assertThat(age).isEqualTo(42);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenFieldDoesNotExist() {
            assertThatThrownBy(() -> KiwiReflection.getFieldValue(jerry, "surname"))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Cannot get field [surname] in object [%s]", jerry)
                    .hasCauseExactlyInstanceOf(NoSuchFieldException.class);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenFieldIsNotAccessible() throws NoSuchFieldException {
            var ageField = jerry.getClass().getDeclaredField("age");
            assertThat(ageField.canAccess(jerry)).isTrue();

            assertThatThrownBy(() -> KiwiReflection.getFieldValue(jerry, ageField))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Cannot get value of field [age] in object [%s]", jerry)
                    .hasCauseExactlyInstanceOf(IllegalAccessException.class);
        }
    }

    @Nested
    class SetFieldValue {

        private Person bob;

        @BeforeEach
        void setUp() {
            bob = new Person("Bob", "Sackamano", 34);
        }

        @Test
        void shouldAcceptFieldName() {
            KiwiReflection.setFieldValue(bob, "firstName", "Robert");
            KiwiReflection.setFieldValue(bob, "age", 27);

            assertThat(bob.getFirstName()).isEqualTo("Robert");
            assertThat(bob.getAge()).isEqualTo(27);
        }

        @Test
        void shouldAcceptFieldObject() {
            var firstNameField = KiwiReflection.findField(bob, "firstName");
            var lastNameField = KiwiReflection.findField(bob, "lastName");
            var ageField = KiwiReflection.findField(bob, "age");

            KiwiReflection.setFieldValue(bob, firstNameField, "Roberto");
            KiwiReflection.setFieldValue(bob, lastNameField, null);
            KiwiReflection.setFieldValue(bob, ageField, 29);

            assertThat(bob.getFirstName()).isEqualTo("Roberto");
            assertThat(bob.getLastName()).isNull();
            assertThat(bob.getAge()).isEqualTo(29);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenFieldIsNotAccessible() throws NoSuchFieldException {
            var ageField = bob.getClass().getDeclaredField("age");

            assertThatThrownBy(() -> KiwiReflection.setFieldValue(bob, ageField, 39))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Error setting field [age] on target [%s] with value [39]", bob)
                    .hasCauseExactlyInstanceOf(IllegalAccessException.class);
        }
    }

    @Nested
    class NonStaticFieldsInHierarchy {

        @Test
        void shouldFindAllFields_WhenClassExtendsOnlyObject() {
            var fields = KiwiReflection.nonStaticFieldsInHierarchy(Person.class);

            assertThat(fields)
                    .extracting(Field::getName)
                    .containsExactlyInAnyOrder("firstName", "lastName", "age");
        }

        @Test
        void shouldFindAllFields_WhenClassInheritsFromSuperclass() {
            var fields = KiwiReflection.nonStaticFieldsInHierarchy(Employee.class);

            assertThat(fields)
                    .extracting(Field::getName)
                    .containsExactlyInAnyOrder("firstName", "lastName", "age", "title");
        }

        @Test
        void shouldNotFindStaticFields() {
            var fields = KiwiReflection.nonStaticFieldsInHierarchy(NoFieldObj.class);

            assertThat(fields).isEmpty();
        }
    }

    @Nested
    class FindPublicAccessorMethods {

        @Test
        void shouldFindPublicGettersAndSetters() {
            var testObject = newAccessorTestClass();
            var methods = KiwiReflection.findPublicAccessorMethods(testObject);

            assertThat(methods)
                    .extracting(Method::getName)
                    .containsExactlyInAnyOrder(
                            "getSomeString",
                            "isPrimitiveBoolean",
                            "getObjectBoolean",
                            "getSomeObjectLong",
                            "getSomePrimitiveLong",
                            "getUnderscoredValue",
                            "getAllArguments"
                    );
        }

        @Test
        void shouldTraverseClassHierarchy() {
            var sub = new SubTestClass();
            var methods = KiwiReflection.findPublicAccessorMethods(sub);

            assertThat(methods)
                    .extracting(Method::getName)
                    .containsExactlyInAnyOrder(
                            "getFoo",
                            "getBar",
                            "getBaz",
                            "getQuux"
                    );
        }
    }

    @Nested
    class IsPublicAccessorOrMutatorMethods {

        @Test
        void shouldIdentityGetOrIsMethods(SoftAssertions softly) {
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getSomeString"))).isTrue();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("isPrimitiveBoolean"))).isTrue();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getObjectBoolean"))).isTrue();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getSomeObjectLong"))).isTrue();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getSomePrimitiveLong"))).isTrue();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getUnderscoredValue"))).isTrue();

            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getNothing"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getFoo"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getBar"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("getBaz"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("computeSomething"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicAccessorMethod(findMethod("doSomething"))).isFalse();
        }

        @Test
        void shouldIdentityStrictlyGetMethods(SoftAssertions softly) {
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("getSomeString"))).isTrue();
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("isPrimitiveBoolean"))).isFalse();
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("getObjectBoolean"))).isTrue();
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("getSomeObjectLong"))).isTrue();
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("getSomePrimitiveLong"))).isTrue();
            softly.assertThat(KiwiReflection.isStrictlyGetAccessorMethod(findMethod("getUnderscoredValue"))).isTrue();
        }

        @Test
        void shouldIdentityStrictlyIsMethods(SoftAssertions softly) {
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("getSomeString"))).isFalse();
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("isPrimitiveBoolean"))).isTrue();
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("getObjectBoolean"))).isFalse();
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("getSomeObjectLong"))).isFalse();
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("getSomePrimitiveLong"))).isFalse();
            softly.assertThat(KiwiReflection.isStrictlyIsAccessorMethod(findMethod("getUnderscoredValue"))).isFalse();
        }

        @Test
        void shouldIdentitySetMethods(SoftAssertions softly) {
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setSomeString", String.class))).isTrue();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setPrimitiveBoolean", boolean.class))).isTrue();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setObjectBoolean", Boolean.class))).isTrue();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setSomeObjectLong", Long.class))).isTrue();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setSomePrimitiveLong", long.class))).isTrue();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setUnderscoredValue", int.class))).isTrue();

            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setNothing"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setFoo", String.class))).isFalse();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setBar", String.class))).isFalse();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("setBaz", String.class))).isFalse();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("computeSomething"))).isFalse();
            softly.assertThat(KiwiReflection.isPublicMutatorMethod(findMethod("doSomething"))).isFalse();
        }

        private Method findMethod(String methodName, Class<?>... paramTypes) {
            return findDeclaredMethodInAccessorTestClass(methodName, paramTypes);
        }
    }

    @Nested
    class FindPublicMutatorMethods {

        @Test
        void shouldFindPublicSetters() {
            var testObject = newAccessorTestClass();
            var methods = KiwiReflection.findPublicMutatorMethods(testObject);

            assertThat(methods)
                    .extracting(Method::getName)
                    .containsExactlyInAnyOrder(
                            "setSomeString",
                            "setPrimitiveBoolean",
                            "setObjectBoolean",
                            "setSomeObjectLong",
                            "setSomePrimitiveLong",
                            "setUnderscoredValue"
                    );
        }

        @Test
        void shouldTraverseClassHierarchy() {
            var sub = new SubTestClass();
            var methods = KiwiReflection.findPublicMutatorMethods(sub);

            assertThat(methods)
                    .extracting(Method::getName)
                    .containsExactlyInAnyOrder(
                            "setFoo",
                            "setBar",
                            "setBaz",
                            "setQuux"
                    );
        }
    }

    @Nested
    class InvokeMutatorMethodsWithNull {

        @Test
        void shouldSetAllPublicReferencePropertiesToNull(SoftAssertions softly) {
            var testObject = AccessorTestClass.builder()
                    .someString("Buenos dias!")
                    .primitiveBoolean(true)
                    .objectBoolean(true)
                    .someObjectLong(84_000L)
                    .somePrimitiveLong(42_000_000L)
                    ._underscoredValue(42)
                    .build();

            KiwiReflection.invokeMutatorMethodsWithNull(testObject);

            softly.assertThat(testObject.isPrimitiveBoolean()).isTrue();
            softly.assertThat(testObject.getSomePrimitiveLong()).isEqualTo(42_000_000L);
            softly.assertThat(testObject.getUnderscoredValue()).isEqualTo(42);

            softly.assertThat(testObject.getSomeString()).isNull();
            softly.assertThat(testObject.getObjectBoolean()).isNull();
            softly.assertThat(testObject.getSomeObjectLong()).isNull();
        }

        @Test
        void shouldTraverseClassHierarchy(SoftAssertions softly) {
            var sub = new SubTestClass();
            sub.setFoo("foo");
            sub.setBaz("bar");
            sub.setBaz("baz");
            sub.setQuux("quux");

            KiwiReflection.invokeMutatorMethodsWithNull(sub);

            softly.assertThat(sub.getFoo()).isNull();
            softly.assertThat(sub.getBar()).isNull();
            softly.assertThat(sub.getBaz()).isNull();
            softly.assertThat(sub.getQuux()).isNull();
        }

        @Test
        void shouldIgnoreReadOnlyProperties(SoftAssertions softly) {
            var bob = new Person("Bob", "Sackamano", 42);

            KiwiReflection.invokeMutatorMethodsWithNull(bob);

            softly.assertThat(bob.getFirstName()).isEqualTo("Bob");
            softly.assertThat(bob.getLastName()).isEqualTo("Sackamano");
            softly.assertThat(bob.getAge()).isEqualTo(42);
        }
    }

    @Nested
    class InvokeMutatorMethodsWithNullIgnoringProperties {

        private MutablePerson alice;

        @BeforeEach
        void setUp() {
            alice = MutablePerson.builder()
                    .firstName("Alice")
                    .lastName("Jones")
                    .age(42)
                    .nickname("Ali")
                    .annualSalary(100_000.0)
                    .netWorth(750_000.0)
                    .state("VA")
                    .zipCode("20194")
                    .alias("Wonderalice")
                    .alias("Handygirl")
                    .build();
        }

        @Test
        void shouldIgnoreSpecifiedProperties(SoftAssertions softly) {
            KiwiReflection.invokeMutatorMethodsWithNullIgnoringProperties(alice,
                    "age", "annualSalary", "netWorth", "zipCode");

            softly.assertThat(alice.getFirstName()).isNull();
            softly.assertThat(alice.getLastName()).isNull();
            softly.assertThat(alice.getNickname()).isNull();
            softly.assertThat(alice.getState()).isNull();
            softly.assertThat(alice.getAliases()).isNull();

            softly.assertThat(alice.getAge()).isEqualTo(42);
            softly.assertThat(alice.getAnnualSalary()).isEqualTo(100_000.0);
            softly.assertThat(alice.getNetWorth()).isEqualTo(750_000.0);
            softly.assertThat(alice.getZipCode()).isEqualTo("20194");
        }

        @Test
        void shouldNullifyAll_WhenNoPropertiesSpecified() {
            KiwiReflection.invokeMutatorMethodsWithNullIgnoringProperties(alice);

            assertThat(alice).isEqualTo(MutablePerson.nullMutablePerson());
        }

        @Test
        void shouldIgnoreBlankPropertyNames() {
            KiwiReflection.invokeMutatorMethodsWithNullIgnoringProperties(alice, "", null, " ");

            assertThat(alice).isEqualTo(MutablePerson.nullMutablePerson());
        }

        @Test
        void shouldIgnoreReadOnlyProperties(SoftAssertions softly) {
            var bob = new Person("Bob", "Sackamano", 42);

            KiwiReflection.invokeMutatorMethodsWithNullIgnoringProperties(bob, "age");

            softly.assertThat(bob.getFirstName()).isEqualTo("Bob");
            softly.assertThat(bob.getLastName()).isEqualTo("Sackamano");
            softly.assertThat(bob.getAge()).isEqualTo(42);
        }
    }

    @Nested
    class InvokeMutatorMethodsWithNullIncludingOnlyProperties {

        private MutablePerson j;

        @BeforeEach
        void setUp() {
            j = MutablePerson.builder()
                    .firstName("James")
                    .lastName("Edwards")
                    .age(32)
                    .nickname("Jay")
                    .annualSalary(50_000.0)
                    .netWorth(75_000.0)
                    .state("NY")
                    .zipCode("10004")
                    .alias("J")
                    .alias("Agent J")
                    .build();
        }

        @Test
        void shouldIncludeOnlySpecifiedProperties(SoftAssertions softly) {
            KiwiReflection.invokeMutatorMethodsWithNullIncludingOnlyProperties(j,
                    "firstName", "lastName", "age", "nickname", "annualSalary", "netWorth", "zipCode");

            softly.assertThat(j.getFirstName()).isNull();
            softly.assertThat(j.getLastName()).isNull();
            softly.assertThat(j.getAge()).isNull();
            softly.assertThat(j.getNickname()).isNull();
            softly.assertThat(j.getAnnualSalary()).isNull();
            softly.assertThat(j.getNetWorth()).isNull();
            softly.assertThat(j.getZipCode()).isNull();

            softly.assertThat(j.getState()).isEqualTo("NY");
            softly.assertThat(j.getAliases()).containsExactlyInAnyOrder("J", "Agent J");
        }

        @Test
        void shouldNullifyNone_WhenNoPropertiesSpecified() {
            var jCopy = j.toBuilder().build();

            KiwiReflection.invokeMutatorMethodsWithNullIncludingOnlyProperties(j);

            assertThat(j).isEqualTo(jCopy);
        }

        @Test
        void shouldIgnoreBlankPropertyNames() {
            var jCopy = j.toBuilder().build();

            KiwiReflection.invokeMutatorMethodsWithNullIncludingOnlyProperties(j, "", null, " ");

            assertThat(j).isEqualTo(jCopy);
        }

        @Test
        void shouldIgnoreReadOnlyProperties(SoftAssertions softly) {
            var bob = new Person("Bob", "Sackamano", 42);

            KiwiReflection.invokeMutatorMethodsWithNullIncludingOnlyProperties(bob,
                    "firstName", "lastName", "age");

            softly.assertThat(bob.getFirstName()).isEqualTo("Bob");
            softly.assertThat(bob.getLastName()).isEqualTo("Sackamano");
            softly.assertThat(bob.getAge()).isEqualTo(42);
        }
    }

    @Nested
    class InvokeMutatorMethodsWithNullSatisfying {

        @Test
        void shouldNullify_MethodsThatSatisfyThePredicate(SoftAssertions softly) {
            var k = MutablePerson.builder()
                    .firstName("Kevin")
                    .lastName("Brown")
                    .nickname("Mr. K")
                    .age(54)
                    .alias("K")
                    .alias("Agent K")
                    .build();

            Predicate<Method> notIntegerParam = not(method -> parameterTypeIsAssignableFrom(method, Integer.class));
            Predicate<Method> notIterableParam = not(method -> parameterTypeIsAssignableFrom(method, Iterable.class));
            Predicate<Method> notIntegerOrIterableParam = notIntegerParam.and(notIterableParam);

            KiwiReflection.invokeMutatorMethodsWithNullSatisfying(k, notIntegerOrIterableParam);

            softly.assertThat(k).usingRecursiveComparison()
                    .ignoringFields("age", "aliases")
                    .isEqualTo(MutablePerson.nullMutablePerson());
            softly.assertThat(k.getAge()).isEqualTo(54);
            softly.assertThat(k.getAliases()).containsExactlyInAnyOrder("K", "Agent K");
        }

        private boolean parameterTypeIsAssignableFrom(Method method, Class<?> type) {
            Class<?> parameterType = method.getParameterTypes()[0];
            return type.isAssignableFrom(parameterType);
        }
    }

    @Data
    @Builder(toBuilder = true)
    static class MutablePerson {
        private String firstName;
        private String lastName;
        private Integer age;
        private String nickname;
        private Double annualSalary;
        private Double netWorth;
        private String state;
        private String zipCode;

        @Singular
        private Set<String> aliases;

        static MutablePerson nullMutablePerson() {
            var person = MutablePerson.builder().build();
            person.setAliases(null);
            return person;
        }
    }

    @Nested
    class FindMethodOptionally {

        private Class<? extends AccessorTestClass> aClass;

        @BeforeEach
        void setUp() {
            aClass = newAccessorTestClass().getClass();
        }

        @ParameterizedTest
        @ValueSource(strings = {"getSomeObjectLong", "getNothing", "isPrimitiveBoolean", "doSomething"})
        void shouldFindPublicNoArgMethods(String methodName) {
            var methodOptional = KiwiReflection.findMethodOptionally(aClass, methodName);

            assertThat(methodOptional).isPresent();
            assertThat(methodOptional.orElseThrow().getName()).isEqualTo(methodName);
        }

        @Test
        void shouldFindPublicMethods_WithArguments() {
            var methodOptional = KiwiReflection.findMethodOptionally(
                    AccessorTestClass.class,
                    "setMultipleValues",
                    String.class, Long.class);

            assertThat(methodOptional).isPresent();

            var method = methodOptional.orElseThrow();
            assertThat(method.getName()).isEqualTo("setMultipleValues");
            assertThat(method.getParameterTypes()).containsExactly(String.class, Long.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"randomMethod", "getFooBar", "tryDoingSomething"})
        void shouldReturnEmptyOptional_WhenMethodNotFound(String methodName) {
            var methodOptional = KiwiReflection.findMethodOptionally(aClass, methodName);

            assertThat(methodOptional).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(strings = {"getFoo", "computeSomething"})
        void shouldNotFindNonPublicMethods(String methodName) throws NoSuchMethodException {
            assertThat(Modifier.isPublic(aClass.getDeclaredMethod(methodName).getModifiers()))
                    .describedAs("Method %s should exist but not be public")
                    .isFalse();

            var methodOptional = KiwiReflection.findMethodOptionally(aClass, methodName);

            assertThat(methodOptional).isEmpty();
        }
    }

    @Nested
    class FindAccessor {

        private AccessorTestClass testObject;

        @BeforeEach
        void setUp() {
            testObject = newAccessorTestClass();
        }

        @Nested
        class ShouldThrowException {

            @ParameterizedTest
            @ValueSource(strings = {"thisDoesNotExist", "spam", "ham"})
            void whenGetAccessorDoesNotExist(String fieldName) {
                // Expecting isXxx because of the fallback (tries getXxx, then isXxx).
                // I doubt it's worth it to change the code to say that getXxx and isXxx weren't found.
                String expectedMethodName = expectedAccessorName("is", fieldName);

                assertGetOrIsAccessor(fieldName, Accessor.GET, expectedMethodName);
            }

            @ParameterizedTest
            @ValueSource(strings = {"thisDoesNotExist", "spoiled", "food"})
            void whenIsAccessorDoesNotExist(String fieldName) {
                String expectedMethodName = expectedAccessorName("is", fieldName);

                assertGetOrIsAccessor(fieldName, Accessor.IS, expectedMethodName);
            }

            private void assertGetOrIsAccessor(String fieldName, Accessor methodType, String expectedMethodName) {
                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(methodType, fieldName, AccessorTestClass.class))
                        .isExactlyInstanceOf(RuntimeReflectionException.class)
                        .hasMessage("Error finding method [%s] in [%s] with params []",
                                expectedMethodName, AccessorTestClass.class)
                        .hasCauseExactlyInstanceOf(NoSuchMethodException.class);
            }

            @ParameterizedTest
            @ValueSource(strings = {"thisDoesNotExist", "green", "eggs"})
            void whenSetAccessorDoesNotExist(String fieldName) {
                String expectedMethodName = expectedAccessorName("set", fieldName);

                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(Accessor.SET, fieldName, AccessorTestClass.class, String.class))
                        .isExactlyInstanceOf(RuntimeReflectionException.class)
                        .hasMessage("Error finding method [%s] in [%s] with params [%s]",
                                expectedMethodName, AccessorTestClass.class, String.class)
                        .hasCauseExactlyInstanceOf(NoSuchMethodException.class);
            }

            @Test
            void whenAttemptToFindGetAccessor_WithAnArgument() {
                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(Accessor.GET, "someString", testObject, Integer.class))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("getter methods must have 0 arguments");
            }

            @Test
            void whenAttemptToFindIsAccessor_WithAnArgument() {
                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(Accessor.IS, "primitiveBoolean", testObject, Integer.class))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("boolean getter methods must have 0 arguments");
            }

            @Test
            void whenAttemptToFindSetAccessor_WithNoArguments() {
                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(Accessor.SET, "someObjectLong", testObject))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("setter methods must have 1 arguments");
            }

            @Test
            void whenAttemptToFindSetAccessor_WithMoreThanOneArgument() {
                assertThatThrownBy(() ->
                        KiwiReflection.findAccessor(Accessor.SET, "objectBoolean", testObject, Boolean.class, String.class))
                        .isExactlyInstanceOf(IllegalArgumentException.class)
                        .hasMessage("setter methods must have 1 arguments");
            }

            private String expectedAccessorName(String set, String fieldName) {
                return set + StringUtils.capitalize(fieldName);
            }
        }

        @Test
        void shouldFindPublicGetters(SoftAssertions softly) {
            var helper = new FindAccessorTestHelper(softly, testObject);

            helper.assertGetterWithExpectedValue("someString", testObject.getSomeString());
            helper.assertGetterWithExpectedValue("someObjectLong", testObject.getSomeObjectLong());
            helper.assertPrimitiveBooleanGetterWithExpectedValue("primitiveBoolean", testObject.isPrimitiveBoolean());
            helper.assertGetterWithExpectedValue("primitiveBoolean", testObject.isPrimitiveBoolean());  // tests fallback to "is"
            helper.assertGetterWithExpectedValue("objectBoolean", testObject.getObjectBoolean());
            helper.assertGetterWithExpectedValue("_underscoredValue", testObject.getUnderscoredValue());
            helper.assertGetterWithExpectedValue("underscoredValue", testObject.getUnderscoredValue());
        }

        @Test
        void shouldFindPublicSetters(SoftAssertions softly) {
            var helper = new FindAccessorTestHelper(softly, testObject);

            helper.assertSetterWithNewValue("someString", "new value");
            helper.assertSetterWithNewValue("someString", String.class, null);

            helper.assertSetterWithNewValue("someObjectLong", 1024L);
            helper.assertSetterWithNewValue("someObjectLong", Long.class, null);

            helper.assertSetterWithNewValue("primitiveBoolean", false);
            helper.assertSetterWithNewValue("primitiveBoolean", boolean.class, true);

            helper.assertSetterWithNewValue("objectBoolean", Boolean.class, null);
            helper.assertSetterWithNewValue("objectBoolean", true);

            helper.assertSetterWithNewValue("_underscoredValue", 84);
            helper.assertSetterWithNewValue("underscoredValue", int.class, 168);
        }
    }

    private static class FindAccessorTestHelper {

        private final SoftAssertions softly;
        private final Object target;

        private FindAccessorTestHelper(SoftAssertions softly, Object target) {
            this.softly = softly;
            this.target = target;
        }

        private void assertGetterWithExpectedValue(String fieldName, @Nonnull Object expectedValue) {
            assertGetterWithExpectedValue(Accessor.GET, fieldName, expectedValue);
        }

        @SuppressWarnings("SameParameterValue")
        private void assertPrimitiveBooleanGetterWithExpectedValue(String fieldName, boolean expectedValue) {
            assertGetterWithExpectedValue(Accessor.IS, fieldName, expectedValue);
        }

        private void assertGetterWithExpectedValue(Accessor accessor, String fieldName, @Nonnull Object expectedValue) {
            checkArgumentNotNull(expectedValue);

            var getterMethod = KiwiReflection.findAccessor(accessor, fieldName, target);
            var value = KiwiReflection.invokeExpectingReturn(getterMethod, target, expectedValue.getClass());

            softly.assertThat(value)
                    .describedAs("invoke " + getterMethod.getName())
                    .isEqualTo(expectedValue);
        }

        private void assertSetterWithNewValue(String fieldName, @Nonnull Object newValue) {
            checkArgumentNotNull(newValue);
            Class<?> newValueClass = newValue.getClass();
            assertSetterWithNewValue(fieldName, newValueClass, newValue);
        }

        private void assertSetterWithNewValue(String fieldName, Class<?> newValueClass, @Nullable Object newValue) {
            var getterMethod = KiwiReflection.findAccessor(Accessor.GET, fieldName, target);
            var originalValue = KiwiReflection.invokeExpectingReturn(getterMethod, target);
            softly.assertThat(originalValue)
                    .describedAs("Expecting new value to be different than original in order to test the setter")
                    .isNotEqualTo(newValue);

            var setterMethod = KiwiReflection.findAccessor(Accessor.SET, fieldName, target, newValueClass);
            KiwiReflection.invokeVoidReturn(setterMethod, target, newValue);
            var actualNewValue = KiwiReflection.invokeExpectingReturn(getterMethod, target);
            softly.assertThat(actualNewValue)
                    .describedAs("Actual new value was not the expected value")
                    .isEqualTo(newValue);
        }
    }

    @Nested
    class FindMethodOrNull {

        /**
         * @implNote Pass null as the method name to cause NPE instead of the usual NoSuchMethodException
         * when {@link Class#getMethod(String, Class[])} is called.
         */
        @WhiteBoxTest
        void shouldCatchAllExceptions() {
            var method = KiwiReflection.findMethodOrNull(AccessorTestClass.class, null, Long.class);

            assertThat(method).isNull();
        }
    }

    @Nested
    class FindMethod {

        private Class<? extends AccessorTestClass> aClass;

        @BeforeEach
        void setUp() {
            aClass = newAccessorTestClass().getClass();
        }

        @ParameterizedTest
        @ValueSource(strings = {"getSomeObjectLong", "getNothing", "isPrimitiveBoolean", "doSomething"})
        void shouldFindPublicNoArgMethods(String methodName) {
            var method = KiwiReflection.findMethod(aClass, methodName);

            assertThat(method.getName()).isEqualTo(methodName);
        }

        @Test
        void shouldFindPublicMethods_WithArguments() {
            var method = KiwiReflection.findMethod(
                    AccessorTestClass.class,
                    "setMultipleValues",
                    String.class, Long.class);

            assertThat(method.getName()).isEqualTo("setMultipleValues");
            assertThat(method.getParameterTypes()).containsExactly(String.class, Long.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"randomMethod", "getFooBar", "tryDoingSomething"})
        void shouldThrowException_WhenMethodNotFound(String methodName) {
            assertThatThrownBy(() -> KiwiReflection.findMethod(aClass, methodName))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Error finding method [%s] in [%s] with params []", methodName, aClass)
                    .hasCauseExactlyInstanceOf(NoSuchMethodException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"getFoo", "computeSomething"})
        void shouldThrowException_ForNonPublicMethods(String methodName) throws NoSuchMethodException {
            assertThat(Modifier.isPublic(aClass.getDeclaredMethod(methodName).getModifiers()))
                    .describedAs("Method %s should exist but not be public")
                    .isFalse();

            assertThatThrownBy(() -> KiwiReflection.findMethod(aClass, methodName))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Error finding method [%s] in [%s] with params []", methodName, aClass)
                    .hasCauseExactlyInstanceOf(NoSuchMethodException.class);
        }
    }

    @Nested
    class InvokeExpectingReturn {

        private AccessorTestClass testObject;

        @BeforeEach
        void setUp() {
            testObject = newAccessorTestClass();
        }

        @Test
        void shouldInvokePublicMethods() {
            var findPersonMethod = findDeclaredMethodInAccessorTestClass("findPerson", Long.class);

            Object value = KiwiReflection.invokeExpectingReturn(findPersonMethod, testObject, 88L);
            assertThat(value).isExactlyInstanceOf(Person.class);

            var person = (Person) value;
            assertThat(person.getFirstName()).isEqualTo("Bob");

            assertArguments(testObject, 88L);
        }

        @Test
        void shouldAcceptExplicitReturnType() {
            var setWhateverMethod = findDeclaredMethodInAccessorTestClass("setWhatever", String.class);

            AccessorTestClass value = KiwiReflection.invokeExpectingReturn(
                    setWhateverMethod, testObject, AccessorTestClass.class, "a string argument");

            assertThat(value).isSameAs(testObject);

            assertArguments(testObject, "a string argument");
        }

        @Test
        void shouldInvokeNonPublicMethods_WhenAccessible() {
            var computeMethod = findDeclaredMethodInAccessorTestClass("computeSomething");

            Object value = KiwiReflection.invokeExpectingReturn(computeMethod, testObject);
            assertThat(value).isEqualTo(testObject.computeSomething());

            assertEmptyArguments(testObject);
        }

        @Test
        void shouldThrowException_WhenIllegalAccess() {
            var secretMethod = findDeclaredMethodInAccessorTestClass("secret");

            assertThatThrownBy(() -> KiwiReflection.invokeExpectingReturn(secretMethod, testObject, String.class))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Error invoking method [secret] on target [%s] with args []", testObject)
                    .hasCauseExactlyInstanceOf(IllegalAccessException.class);

            assertEmptyArguments(testObject);
        }
    }

    @Nested
    class InvokeVoidReturn {

        private AccessorTestClass testObject;

        @BeforeEach
        void setUp() {
            testObject = newAccessorTestClass();
        }

        @Test
        void shouldInvokePublicVoidMethods_WithNoArguments() {
            var doSomethingMethod = findDeclaredMethodInAccessorTestClass("doSomething");

            assertThatCode(() -> KiwiReflection.invokeVoidReturn(doSomethingMethod, testObject))
                    .doesNotThrowAnyException();

            assertEmptyArguments(testObject);
        }

        @Test
        void shouldInvokePublicVoidMethods_WithArguments() {
            var savePersonMethod = findDeclaredMethodInAccessorTestClass("savePerson", Person.class);

            var person = new Person("George", "Costanza", 43);

            assertThatCode(() -> KiwiReflection.invokeVoidReturn(savePersonMethod, testObject, person))
                    .doesNotThrowAnyException();

            assertArguments(testObject, person);
        }

        @Test
        void shouldInvokeNonPublicMethods_WhenAccessible() {
            var protectedMethod = findDeclaredMethodInAccessorTestClass("protectedMethod",
                    String.class, int.class, boolean.class);

            assertThatCode(() -> KiwiReflection.invokeVoidReturn(protectedMethod,
                    testObject,
                    "string", 42, true))
                    .doesNotThrowAnyException();

            assertArguments(testObject, "string", 42, true);
        }

        @Test
        void shouldThrowException_WhenIllegalAccess() {
            var setFooMethod = findDeclaredMethodInAccessorTestClass("setFoo", String.class);

            assertThatThrownBy(() -> KiwiReflection.invokeVoidReturn(setFooMethod, testObject, "bar"))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasMessage("Error invoking void method [setFoo] on target [%s] with args [bar]",
                            testObject)
                    .hasCauseExactlyInstanceOf(IllegalAccessException.class);
        }
    }

    private static void assertArguments(AccessorTestClass testObject, Object... expectedArgs) {
        assertThat(testObject.getAllArguments()).containsExactly(expectedArgs);
    }

    private static void assertEmptyArguments(AccessorTestClass testObject) {
        assertThat(testObject.getAllArguments()).isEmpty();
    }

    private static AccessorTestClass newAccessorTestClass() {
        return AccessorTestClass.builder()
                .someString("some string")
                .someObjectLong(84L)
                .somePrimitiveLong(42L)
                .primitiveBoolean(true)
                .objectBoolean(false)
                ._underscoredValue(42)  // yes, the Lombok builder has a leading _
                .build();
    }

    private static Method findDeclaredMethodInAccessorTestClass(String name, Class<?>... paramTypes) {
        try {
            return AccessorTestClass.class.getDeclaredMethod(name, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    @Slf4j
    @Getter
    @Setter
    @Builder
    @ToString
    static class AccessorTestClass {

        private String someString;
        private boolean primitiveBoolean;
        private Boolean objectBoolean;
        private Long someObjectLong;
        private long somePrimitiveLong;

        // Contains ALL arguments called on this instance, in the order in which the corresponding methods were called
        @Setter(AccessLevel.NONE)
        @Builder.Default
        private List<Object> allArguments = new ArrayList<>();

        @Getter(AccessLevel.NONE)
        @Setter(AccessLevel.NONE)
        private int _underscoredValue;

        public int getUnderscoredValue() {
            return _underscoredValue;
        }

        public void setUnderscoredValue(int value) {
            addArgs(value);
            _underscoredValue = value;
        }

        // "setter" with multiple arguments (so not really a setter in JavaBeans sense)
        public void setMultipleValues(String newSomeString, Long newSomeObjectLong) {
            addArgs(newSomeObjectLong, newSomeObjectLong);
            this.someString = newSomeString;
            this.someObjectLong = newSomeObjectLong;
        }

        // "setters" that returns "this" (so not really a setter in JavaBeans sense)
        public AccessorTestClass setWhatever(String newValue) {
            addArgs(newValue);
            this.someString = newValue;
            return this;
        }

        // "getter" with an argument
        public String getWhatever(int arg2) {
            addArgs(arg2);
            return "whatever";
        }

        // "getter" with multiple arguments
        public String getWhatever(String arg1, int arg2) {
            addArgs(arg1, arg2);
            return "whatever";
        }

        public void getNothing() {
            // "getter" with void return
        }

        public void setNothing() {
            // "setter" with no argument
        }

        // private getter
        private String getFoo() {
            throw new UnsupportedOperationException();
        }

        // private setter
        private void setFoo(String foo) {
            addArgs(foo);
            throw new UnsupportedOperationException();
        }

        // getter with no modifier
        String getBar() {
            throw new UnsupportedOperationException();
        }

        // setter with no modifier
        void setBar(String bar) {
            addArgs(bar);
            throw new UnsupportedOperationException();
        }

        // protected getter
        protected String getBaz() {
            throw new UnsupportedOperationException();
        }

        // protected setter
        protected void setBaz(String baz) {
            addArgs(baz);
            throw new UnsupportedOperationException();
        }

        int computeSomething() {
            LOG.debug("computing the answer...");
            return 42;
        }

        public void doSomething() {
            LOG.debug("doing something...");
        }

        public Person findPerson(Long id) {
            addArgs(id);
            LOG.debug("Finding person {}", id);

            return new Person("Bob", "Sackamano", 42);
        }

        public void savePerson(Person person) {
            addArgs(person);
            LOG.debug("Saving person {} {}", person.getFirstName(), person.getLastName());
        }

        protected void protectedMethod(String arg1, int arg2, boolean arg3) {
            addArgs(arg1, arg2, arg3);
            // GNDN (goes nowhere, does nothing...)
        }

        private String secret() {
            return "secret";
        }

        private void addArgs(Object... newArgs) {
            allArguments.addAll(List.of(newArgs));
        }
    }

    @Nested
    class NewInstanceUsingNoArgsConstructor {

        @Test
        void shouldCreateNewInstance() {
            var emptyPerson = KiwiReflection.newInstanceUsingNoArgsConstructor(Person.class);

            assertThat(emptyPerson).isNotNull();
            assertThat(emptyPerson.getFirstName()).isNull();
            assertThat(emptyPerson.getLastName()).isNull();
            assertThat(emptyPerson.getAge()).isNull();
        }

        @Test
        void shouldThrowIllegalArgument_WhenTypeIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceUsingNoArgsConstructor(null))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_WhenNoArgsConstructorDoesNotExist() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceUsingNoArgsConstructor(User.class))
                    .withCauseInstanceOf(NoSuchMethodException.class)
                    .withMessage("%s does not have a declared no-args constructor", User.class);
        }

        @Test
        void shouldThrowIllegalArgument_WhenNoArgsConstructorIsPrivate() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceUsingNoArgsConstructor(SomeUtilities.class))
                    .withCauseInstanceOf(IllegalAccessException.class)
                    .withMessage("%s does not have a declared no-args constructor", SomeUtilities.class);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenProblemInvokingConstructor() {
            assertThatThrownBy(() -> KiwiReflection.newInstanceUsingNoArgsConstructor(Erroneous.class))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasCauseInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    class NewInstanceInferringParamTypes {

        @Test
        void shouldCreateWhenThereAreNoArguments() {
            var user = KiwiReflection.newInstanceInferringParamTypes(Person.class);

            assertThat(user).isNotNull();
            assertThat(user.getFirstName()).isNull();
            assertThat(user.getLastName()).isNull();
            assertThat(user.getAge()).isNull();
        }

        @Test
        void shouldCreateWhenArgumentsMatchParameterTypesExactly_TwoStringArgs() {
            var alice = KiwiReflection.newInstanceInferringParamTypes(User.class,
                    "alice.jones@nowhere.com", "monkey-123!");

            assertThat(alice).isNotNull();
            assertThat(alice.getUsername()).isEqualTo("alice.jones@nowhere.com");
            assertThat(alice.getPassword()).isEqualTo("monkey-123!");
            assertThat(alice.getEmail()).isEqualTo("alice.jones@nowhere.com");
        }

        @Test
        void shouldCreateWhenArgumentsMatchParameterTypesExactly_TwoArgs() {
            var alice = KiwiReflection.newInstanceInferringParamTypes(User.class,
                    "alice.jones@nowhere.com", "monkey-123!".getBytes());

            assertThat(alice).isNotNull();
            assertThat(alice.getUsername()).isEqualTo("alice.jones@nowhere.com");
            assertThat(alice.getPassword()).isEqualTo("monkey-123!");
            assertThat(alice.getEmail()).isEqualTo("alice.jones@nowhere.com");
        }

        @Test
        void shouldCreateWhenArgumentsMatchParameterTypesExactly_ThreeArgs() {
            var alice = KiwiReflection.newInstanceInferringParamTypes(User.class,
                    "alice.jones@nowhere.com", "monkey-123!".getBytes(), "alicej");

            assertThat(alice).isNotNull();
            assertThat(alice.getUsername()).isEqualTo("alicej");
            assertThat(alice.getPassword()).isEqualTo("monkey-123!");
            assertThat(alice.getEmail()).isEqualTo("alice.jones@nowhere.com");
        }

        @Test
        void shouldCreateWhenArgumentsCanBeAssignedToParameterTypes() {
            var alice = KiwiReflection.newInstanceInferringParamTypes(User.class,
                    "alice.jones@nowhere.com", "monkey-123!", "alicej");

            assertThat(alice).isNotNull();
            assertThat(alice.getUsername()).isEqualTo("alicej");
            assertThat(alice.getPassword()).isEqualTo("monkey-123!");
            assertThat(alice.getEmail()).isEqualTo("alice.jones@nowhere.com");
        }

        @Test
        void shouldThrowIllegalArgument_IfTypeIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(null, "arg1", "arg2"))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_IfArgumentsIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(User.class, (Object[]) null))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_WhenNoMatchingConstructor() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(User.class, "bob"))
                    .withNoCause()
                    .withMessage("No declared constructor found for argument types: %s", List.of(String.class));
        }

        @Test
        void shouldThrowIllegalArgument_WhenNoMatchingConstructor_DueToPrimitiveConstructorParameter() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(Primitive.class, "foo", 84))
                    .withNoCause()
                    .withMessage("No declared constructor found for argument types: %s", List.of(String.class, Integer.class));
        }

        @Test
        void shouldThrowNullPointerException_IfAnyArgumentIsNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(User.class,"carlos@acme.com", "carlos_g", null))
                    .withNoCause()
                    .withMessage("Cannot infer types because one (or more) arguments is null");
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenProblemInvokingConstructor() {
            assertThatThrownBy(() -> KiwiReflection.newInstanceInferringParamTypes(Erroneous.class, "foo", 42))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasCauseInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    class NewInstanceUsingParameterTypesAndArguments {

        @Test
        void shouldCreateForNoArgsConstructor() {
            var emptyPerson = KiwiReflection.newInstance(Person.class, List.of(), List.of());

            assertThat(emptyPerson).isNotNull();
            assertThat(emptyPerson.getFirstName()).isNull();
            assertThat(emptyPerson.getLastName()).isNull();
            assertThat(emptyPerson.getAge()).isNull();
        }

        @Test
        void shouldCreateWhenContainsMatchingConstructor() {
            var paramTypes = List.<Class<?>>of(String.class, byte[].class, String.class);
            var args = List.<Object>of("bsmith@foo.com", "password!".getBytes(), "bsmith");

            var bob = KiwiReflection.newInstance(User.class, paramTypes, args);

            assertThat(bob).isNotNull();
            assertThat(bob.getUsername()).isEqualTo("bsmith");
            assertThat(bob.getPassword()).isEqualTo("password!");
            assertThat(bob.getEmail()).isEqualTo("bsmith@foo.com");
        }

        @Test
        void shouldCreateWhenAnArgumentIsNull() {
            var paramTypes = List.<Class<?>>of(String.class, byte[].class, String.class);
            var args = Lists.<Object>newArrayList("bsmith@foo.com", "password!".getBytes(), null);

            var bob = KiwiReflection.newInstance(User.class, paramTypes, args);

            assertThat(bob).isNotNull();
            assertThat(bob.getUsername()).isEqualTo("bsmith@foo.com");
            assertThat(bob.getPassword()).isEqualTo("password!");
            assertThat(bob.getEmail()).isEqualTo("bsmith@foo.com");
        }

        @Test
        void shouldThrowIllegalArgument_WhenTypeIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(null, List.of(), List.of()))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_WhenParameterTypesIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(User.class, null, List.of("alice@testing.org", "monkey_789")))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_WhenArgumentsIsNull() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(User.class, List.of(String.class, String.class), null))
                    .withNoCause();
        }

        @Test
        void shouldThrowIllegalArgument_WhenParamTypesAndArgsLengthMismatch() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(User.class, List.of(String.class, String.class), List.of("bob@acme.org")))
                    .withNoCause()
                    .withMessage("parameter types and arguments must have same size");
        }

        @Test
        void shouldThrowIllegalArgument_WhenNoMatchingConstructor() {
            List<Class<?>> parameterTypes = List.of(String.class);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(User.class, parameterTypes, List.of("carlos@testing.org")))
                    .withCauseInstanceOf(NoSuchMethodException.class)
                    .withMessage("No declared constructor exists in %s for parameter types: %s", User.class, parameterTypes);
        }

        @Test
        void shouldThrowIllegalArgument_WhenConstructorIsPrivate() {
            List<Class<?>> parameterTypes = List.of(Integer.TYPE);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstance(AddingFactory.class, parameterTypes, List.of(42)))
                    .withCauseInstanceOf(IllegalAccessException.class)
                    .withMessage("No declared constructor exists in %s for parameter types: %s", AddingFactory.class, parameterTypes);
        }

        @Test
        void shouldThrowRuntimeReflectionException_WhenProblemInvokingConstructor() {
            List<Class<?>> parameterTypes = List.of(String.class, Integer.class);
            List<Object> args = List.of("foo", 42);
            assertThatThrownBy(() -> KiwiReflection.newInstance(Erroneous.class, parameterTypes, args))
                    .isExactlyInstanceOf(RuntimeReflectionException.class)
                    .hasCauseInstanceOf(InvocationTargetException.class);
        }
    }

    @Nested
    class NewInstanceUsingParameterTypesAndVariableArguments {

        @Test
        void shouldDelegateToNewInstanceWithTwoLists() {
            var paramTypes = List.<Class<?>>of(String.class, byte[].class, String.class);
            var alice = KiwiReflection.newInstanceExactParamTypes(User.class,
                    paramTypes, "ajones@acme.com", "1-p-2-a-3-ssword!".getBytes(), "ajones");

            assertThat(alice).isNotNull();
            assertThat(alice.getUsername()).isEqualTo("ajones");
            assertThat(alice.getPassword()).isEqualTo("1-p-2-a-3-ssword!");
            assertThat(alice.getEmail()).isEqualTo("ajones@acme.com");
        }

        @Test
        void shouldThrowIllegalArgument_WhenArgumentsIsNull() {
            var parameterTypes = List.<Class<?>>of(String.class, byte[].class, String.class);
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> KiwiReflection.newInstanceExactParamTypes(User.class, parameterTypes, (Object[]) null))
                    .withNoCause();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    static class Person {
        private String firstName;
        private String lastName;
        private Integer age;
    }

    @AllArgsConstructor
    @Getter
    static class Employee extends Person {
        private final String title;
    }

    @Getter
    static class User {
        private String username;
        private String password;
        private String email;

        User(String email, String password) {
            this(email, password, email);
        }

        User(String email, byte[] password) {
            this(email, password, email);
        }

        User(String email, byte[] password, @Nullable String username) {
            this.email = email.toString();
            try {
                this.password = new String(password, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new UncheckedIOException(e);
            }
            this.username = firstNonNull(username, email);
        }

        User(CharSequence email, CharSequence password, @Nullable CharSequence username) {
            this.email = email.toString();
            this.password = password.toString();
            this.username = firstNonNull(username, email);
        }

        private static String firstNonNull(CharSequence seq1, CharSequence seq2) {
            if (isNull(seq1) && isNull(seq2)) {
                throw new IllegalArgumentException("both arguments are null!");
            }

            return isNull(seq1) ? seq2.toString() : seq1.toString();
        }
    }

    @UtilityClass
    static class SomeUtilities {

        int add(int x, int y) {
            return x + y;
        }
    }

    static class AddingFactory {

        private final int base;

        private AddingFactory(int base) {
            this.base = base;
        }

        static AddingFactory newAddingFactory(int base) {
            return newAddingFactory(base);
        }

        int add(int other) {
            return base + other;
        }
    }

    static class Erroneous {
        Erroneous() {
            throw new IllegalStateException();
        }

        Erroneous(String s, Integer i) {
            throw new RuntimeException("oops");
        }
    }

    static class Primitive {
        Primitive(String s, int i) {
        }
    }

    // Suppress several warnings since the fields are not used, and one of the static fields is intentionally not final
    @SuppressWarnings({"unused", "FieldMayBeFinal"})
    @AllArgsConstructor
    @Getter
    static class NoFieldObj {
        private static final String I_AM_A_CONSTANT = "Foo";
        private static String nogoField = "Nope!";
        public static int counter = 42;
    }

    @Getter
    @Setter
    static class SimpleTestClass {
        private String foo;
        private String bar;
    }

    @Getter
    @Setter
    static class SubTestClass extends SimpleTestClass {
        private String baz;
        private String quux;
    }
}
