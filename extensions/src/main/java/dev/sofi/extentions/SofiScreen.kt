package dev.sofi.extentions

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
annotation class SofiScreen(
    val name: String = ""
)