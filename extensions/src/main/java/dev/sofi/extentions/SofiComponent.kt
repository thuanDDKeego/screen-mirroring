package dev.sofi.extentions

import kotlin.reflect.KClass

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(
    // function declarations
    // @SofiComponent fun Foo() { ... }
    // lambda expressions
    // val foo = @SofiComponent { ... }
    AnnotationTarget.FUNCTION,

    // type declarations
    // var foo: @SofiComponent () -> Unit = { ... }
    // parameter types
    // foo: @SofiComponent () -> Unit
    AnnotationTarget.TYPE,

    // composable types inside of type signatures
    // foo: (@SofiComponent () -> Unit) -> Unit
    AnnotationTarget.TYPE_PARAMETER,

    // composable property getters and setters
    // val foo: Int @SofiComponent get() { ... }
    // var bar: Int
    //   @SofiComponent get() { ... }
    AnnotationTarget.PROPERTY_GETTER
)
annotation class SofiComponent(
    val private: Boolean = false,
    val requires: Array<KClass<*>> = [],
    /**
     * Some components are able to preview & recompose at the same time
     */
    val canPreview: Boolean = false,
    /**
     * When putting useFor on the composable, we can easily search it in IDE by screen name, it's helpful, maybe..
     */
    val useFor: Array<String> = [],
)