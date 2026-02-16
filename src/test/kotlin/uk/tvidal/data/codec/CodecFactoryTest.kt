package uk.tvidal.data.codec

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.tvidal.data.database.Child
import uk.tvidal.data.database.Parent

class CodecFactoryTest {

  val factory = CodecFactory()

  @Test
  fun constructorEntityDecoder() {
    val decoder = factory.decoder(Parent::class, null)
    assertThat { decoder is EntityDecoder.ByConstructor }

    val byConstructor = decoder as EntityDecoder.ByConstructor
    assertThat(byConstructor.constructor).isEqualTo(::Parent)

    val paramDecoders = byConstructor.parameterDecoders
    val nameDecoder = paramDecoders.first()
    assertThat(nameDecoder.parameter).isEqualTo(::Parent.parameters.first())

    val idDecoder = paramDecoders.last()
    assertThat(idDecoder.parameter).isEqualTo(::Parent.parameters.last())
  }

  @Test
  fun compositeEntityDecoder() {
    val decoder = factory.decoder(Child::class, null)
    assertThat { decoder is EntityDecoder.ByConstructor }

    val byConstructor = decoder as EntityDecoder.ByConstructor
    assertThat(byConstructor.constructor).isEqualTo(::Child)

    val parent = byConstructor.parameterDecoders.first()
    assertThat(parent.decode is EntityDecoder.ByConstructor).isTrue

    val parentDecoder = parent.decode as EntityDecoder.ByConstructor
    assertThat(
      parentDecoder.parameterDecoders.all {
        (it.decode as EntityDecoder.FieldDecoder<*>)
          .fieldName
          .startsWith("parent_")
      }
    ).isTrue
  }
}
