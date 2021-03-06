/*
 * Copyright (C) 2020 Square, Inc.
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
package com.squareup.wire.schema

import com.squareup.wire.schema.ProtoFile.Syntax
import com.squareup.wire.schema.ProtoFile.Syntax.PROTO_2
import com.squareup.wire.schema.ProtoFile.Syntax.PROTO_3

/** A set of rules which defines schema requirements for a specific [Syntax]. */
interface SyntaxRules {
  fun allowUserDefinedDefaultValue(): Boolean
  fun canExtend(protoType: ProtoType): Boolean
  fun enumRequiresZeroValueAtFirstPosition(): Boolean
  fun isPackedByDefault(type: ProtoType, label: Field.Label?): Boolean
  fun getEncodeMode(
    protoType: ProtoType,
    label: Field.Label?,
    isPacked: Boolean
  ): Field.EncodeMode

  companion object {
    fun get(syntax: Syntax?): SyntaxRules {
      return when (syntax) {
        PROTO_3 -> PROTO_3_SYNTAX_RULES
        PROTO_2,
        null -> PROTO_2_SYNTAX_RULES
      }
    }

    internal val PROTO_2_SYNTAX_RULES = object : SyntaxRules {
      override fun allowUserDefinedDefaultValue(): Boolean = true
      override fun canExtend(protoType: ProtoType): Boolean = true
      override fun enumRequiresZeroValueAtFirstPosition(): Boolean = false
      override fun isPackedByDefault(
        type: ProtoType,
        label: Field.Label?
      ): Boolean = false

      override fun getEncodeMode(
        protoType: ProtoType,
        label: Field.Label?,
        isPacked: Boolean
      ): Field.EncodeMode {
        return when (label) {
          Field.Label.REPEATED ->
            if (isPacked) Field.EncodeMode.PACKED
            else Field.EncodeMode.REPEATED
          Field.Label.OPTIONAL -> Field.EncodeMode.NULL_IF_ABSENT
          Field.Label.REQUIRED -> Field.EncodeMode.THROW_IF_ABSENT
          Field.Label.ONE_OF,
          null -> if (protoType.isMap) Field.EncodeMode.MAP else Field.EncodeMode.NULL_IF_ABSENT
        }
      }
    }

    internal val PROTO_3_SYNTAX_RULES = object : SyntaxRules {
      override fun allowUserDefinedDefaultValue(): Boolean = false
      override fun canExtend(protoType: ProtoType): Boolean {
        return protoType in Options.GOOGLE_PROTOBUF_OPTION_TYPES
      }

      override fun enumRequiresZeroValueAtFirstPosition(): Boolean = true
      override fun isPackedByDefault(
        type: ProtoType,
        label: Field.Label?
      ): Boolean {
        return label == Field.Label.REPEATED && type in ProtoType.NUMERIC_SCALAR_TYPES
      }

      override fun getEncodeMode(
        protoType: ProtoType,
        label: Field.Label?,
        isPacked: Boolean
      ): Field.EncodeMode {
        if (label == Field.Label.REPEATED) {
          return if (isPacked) {
            Field.EncodeMode.PACKED
          } else {
            Field.EncodeMode.REPEATED
          }
        }
        if (protoType.isMap) return Field.EncodeMode.MAP

        return Field.EncodeMode.IDENTITY_IF_ABSENT
      }
    }
  }
}
