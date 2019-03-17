/*
 * Copyright (C) 2014 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.wire.schema.internal.parser

import com.google.common.collect.ImmutableList
import com.google.common.collect.Range
import com.squareup.wire.schema.Field.Label.OPTIONAL
import com.squareup.wire.schema.Field.Label.REPEATED
import com.squareup.wire.schema.Field.Label.REQUIRED
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.internal.parser.OptionElement.Kind
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MessageElementTest {
  internal var location = Location.get("file.proto")
  @Test
  fun emptyToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message"
    )
    val expected = "message Message {}\n"
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun simpleToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(
                FieldElement.builder(location)
                    .label(REQUIRED)
                    .type("string")
                    .name("name")
                    .tag(1)
                    .build()
            )
        )
    val expected = """
        |message Message {
        |  required string name = 1;
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun addMultipleFields() {
    val firstName = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("first_name")
        .tag(1)
        .build()
    val lastName = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("last_name")
        .tag(2)
        .build()
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(firstName, lastName)
    )
    assertThat(element.fields).hasSize(2)
  }

  @Test
  fun simpleWithDocumentationToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        documentation = "Hello",
        fields = listOf(
            FieldElement.builder(location)
                .label(REQUIRED)
                .type("string")
                .name("name")
                .tag(1)
                .build()
        )
    )
    val expected = """
        |// Hello
        |message Message {
        |  required string name = 1;
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun simpleWithOptionsToSchema() {
    val field = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .build()
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(field),
        options = listOf(OptionElement.create("kit", Kind.STRING, "kat"))
    )
    val expected =
        """message Message {
        |  option kit = "kat";
        |
        |  required string name = 1;
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun addMultipleOptions() {
    val field = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .build()
    val kitKat = OptionElement.create("kit", Kind.STRING, "kat")
    val fooBar = OptionElement.create("foo", Kind.STRING, "bar")
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(field),
        options = listOf(kitKat, fooBar)
    )
    assertThat(element.options).hasSize(2)
  }

  @Test
  fun simpleWithNestedElementsToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(
            FieldElement.builder(location)
                .label(REQUIRED)
                .type("string")
                .name("name")
                .tag(1)
                .build()
        ),
        nestedTypes = listOf(
            MessageElement(
                location = location,
                name = "Nested",
                fields = listOf(
                    FieldElement.builder(location)
                        .label(REQUIRED)
                        .type("string")
                        .name("name")
                        .tag(1)
                        .build()
                )
            )
        )
    )
    val expected = """
        |message Message {
        |  required string name = 1;
        |
        |  message Nested {
        |    required string name = 1;
        |  }
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun addMultipleTypes() {
    val nested1 = MessageElement(
        location = location,
        name = "Nested1")
    val nested2 = MessageElement(
        location = location,
        name = "Nested2")
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(
            FieldElement.builder(location)
                .label(REQUIRED)
                .type("string")
                .name("name")
                .tag(1)
                .build()
        ),
        nestedTypes = listOf(nested1, nested2)
    )
    assertThat(element.nestedTypes).hasSize(2)
  }

  @Test
  fun simpleWithExtensionsToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(
            FieldElement.builder(location)
                .label(REQUIRED)
                .type("string")
                .name("name")
                .tag(1)
                .build()
        ),
        extensions = listOf(
            ExtensionsElement.create(location, 500, 501, "")
        )
    )
    val expected = """
        |message Message {
        |  required string name = 1;
        |
        |  extensions 500 to 501;
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun addMultipleExtensions() {
    val fives = ExtensionsElement.create(location, 500, 501, "")
    val sixes = ExtensionsElement.create(location, 600, 601, "")
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(
            FieldElement.builder(location)
                .label(REQUIRED)
                .type("string")
                .name("name")
                .tag(1)
                .build()
        ),
        extensions = listOf(fives, sixes)
    )
    assertThat(element.extensions).hasSize(2)
  }

  @Test
  fun oneOfToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        oneOfs = listOf(
            OneOfElement.builder()
                .name("hi")
                .fields(
                    ImmutableList.of(
                        FieldElement.builder(location)
                            .type("string")
                            .name("name")
                            .tag(1)
                            .build()
                    )
                )
                .build()
        )
    )
    val expected = """
        |message Message {
        |  oneof hi {
        |    string name = 1;
        |  }
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun oneOfWithGroupToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        oneOfs = listOf(
            OneOfElement.builder()
                .name("hi")
                .fields(
                    ImmutableList.of(
                        FieldElement.builder(location)
                            .type("string")
                            .name("name")
                            .tag(1)
                            .build()
                    )
                )
                .groups(
                    ImmutableList.of(
                        GroupElement.builder(location.at(5, 5))
                            .name("Stuff")
                            .tag(3)
                            .fields(
                                ImmutableList.of(
                                    FieldElement.builder(location.at(6, 7))
                                        .label(OPTIONAL)
                                        .type("int32")
                                        .name("result_per_page")
                                        .tag(4)
                                        .build(),
                                    FieldElement.builder(location.at(7, 7))
                                        .label(OPTIONAL)
                                        .type("int32")
                                        .name("page_count")
                                        .tag(5)
                                        .build()
                                )
                            )
                            .build()
                    )
                )
                .build()
        )
    )
    val expected = """
        |message Message {
        |  oneof hi {
        |    string name = 1;
        |  
        |    group Stuff = 3 {
        |      optional int32 result_per_page = 4;
        |      optional int32 page_count = 5;
        |    }
        |  }
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun addMultipleOneOfs() {
    val hi = OneOfElement.builder()
        .name("hi")
        .fields(
            ImmutableList.of(
                FieldElement.builder(location)
                    .type("string")
                    .name("name")
                    .tag(1)
                    .build()
            )
        )
        .build()
    val hey = OneOfElement.builder()
        .name("hey")
        .fields(
            ImmutableList.of(
                FieldElement.builder(location)
                    .type("string")
                    .name("city")
                    .tag(2)
                    .build()
            )
        )
        .build()
    val element = MessageElement(
        location = location,
        name = "Message",
        oneOfs = listOf(hi, hey)
    )
    assertThat(element.oneOfs).hasSize(2)
  }

  @Test
  fun reservedToSchema() {
    val element = MessageElement(
        location = location,
        name = "Message",
        reserveds = listOf(
            ReservedElement.create(
                location, "", ImmutableList.of(10, Range.closed(12, 14), "foo")
            ),
            ReservedElement.create(location, "", ImmutableList.of(10)),
            ReservedElement.create(location, "", ImmutableList.of(Range.closed(12, 14))),
            ReservedElement.create(location, "", ImmutableList.of("foo"))
        )
    )
    val expected = """
        |message Message {
        |  reserved 10, 12 to 14, "foo";
        |  reserved 10;
        |  reserved 12 to 14;
        |  reserved "foo";
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun groupToSchema() {
    val element = MessageElement(
        location = location.at(1, 1),
        name = "SearchResponse",
        groups = listOf(
            GroupElement.builder(location.at(2, 3))
                .label(REPEATED)
                .name("Result")
                .tag(1)
                .fields(
                    ImmutableList.of(
                        FieldElement.builder(location.at(3, 5))
                            .label(REQUIRED)
                            .type("string")
                            .name("url")
                            .tag(2)
                            .build(),
                        FieldElement.builder(location.at(4, 5))
                            .label(OPTIONAL)
                            .type("string")
                            .name("title")
                            .tag(3)
                            .build(),
                        FieldElement.builder(location.at(5, 5))
                            .label(REPEATED)
                            .type("string")
                            .name("snippets")
                            .tag(4)
                            .build()
                    )
                )
                .build()
        )
    )
    val expected = """
        |message SearchResponse {
        |  repeated group Result = 1 {
        |    required string url = 2;
        |    optional string title = 3;
        |    repeated string snippets = 4;
        |  }
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun multipleEverythingToSchema() {
    val field1 = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .build()
    val field2 = FieldElement.builder(location)
        .label(REQUIRED)
        .type("bool")
        .name("other_name")
        .tag(2)
        .build()
    val oneOf1Field = FieldElement.builder(location)
        .type("string")
        .name("namey")
        .tag(3)
        .build()
    val oneOf1 = OneOfElement.builder()
        .name("thingy")
        .fields(ImmutableList.of(oneOf1Field))
        .build()
    val oneOf2Field = FieldElement.builder(location)
        .type("string")
        .name("namer")
        .tag(4)
        .build()
    val oneOf2 = OneOfElement.builder()
        .name("thinger")
        .fields(ImmutableList.of(oneOf2Field))
        .build()
    val extensions1 = ExtensionsElement.create(location, 500, 501, "")
    val extensions2 = ExtensionsElement.create(location, 503, 503, "")
    val nested = MessageElement(
        location = location,
        name = "Nested",
        fields = listOf(field1)
    )
    val option = OptionElement.create("kit", Kind.STRING, "kat")
    val element = MessageElement(
        location = location,
        name = "Message",
        fields = listOf(field1, field2),
        oneOfs = listOf(oneOf1, oneOf2),
        nestedTypes = listOf(nested),
        extensions = listOf(extensions1, extensions2),
        options = listOf(option)
    )
    val expected = """
        |message Message {
        |  option kit = "kat";
        |
        |  required string name = 1;
        |  required bool other_name = 2;
        |
        |  oneof thingy {
        |    string namey = 3;
        |  }
        |  oneof thinger {
        |    string namer = 4;
        |  }
        |
        |  extensions 500 to 501;
        |  extensions 503;
        |
        |  message Nested {
        |    required string name = 1;
        |  }
        |}
        |""".trimMargin()
    assertThat(element.toSchema()).isEqualTo(expected)
  }

  @Test
  fun fieldToSchema() {
    val field = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .build()
    val expected = "required string name = 1;\n"
    assertThat(field.toSchema()).isEqualTo(expected)
  }

  @Test
  fun oneOfFieldToSchema() {
    val field = FieldElement.builder(location)
        .type("string")
        .name("name")
        .tag(1)
        .build()
    val expected = "string name = 1;\n"
    assertThat(field.toSchema()).isEqualTo(expected)
  }

  @Test
  fun fieldWithDocumentationToSchema() {
    val field = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .documentation("Hello")
        .build()
    val expected =
        """// Hello
        |required string name = 1;
        |""".trimMargin()
    assertThat(field.toSchema()).isEqualTo(expected)
  }

  @Test
  fun fieldWithOptionsToSchema() {
    val field = FieldElement.builder(location)
        .label(REQUIRED)
        .type("string")
        .name("name")
        .tag(1)
        .options(ImmutableList.of(OptionElement.create("kit", Kind.STRING, "kat")))
        .build()
    val expected =
        """required string name = 1 [
        |  kit = "kat"
        |];
        |""".trimMargin()
    assertThat(field.toSchema()).isEqualTo(expected)
  }
}
