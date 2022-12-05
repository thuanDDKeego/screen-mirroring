package dev.sofi.extentions

import kotlin.reflect.KClass

@MustBeDocumented
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE
)
public annotation class SofiBinding(
    val note: String = "",
    /**
     * Truyen vao cac class cha dang chiu trach nhiem binding du lieu cho property nay
     */
    @get:Suppress("ArrayReturn") // Kotlin generates a raw array for annotation vararg
    vararg val sources: KClass<out Annotation>
)
