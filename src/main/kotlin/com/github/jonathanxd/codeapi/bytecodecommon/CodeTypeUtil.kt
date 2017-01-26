/*
 *      CodeAPI-BytecodeCommon - Framework to generate Java code and Bytecode code. <https://github.com/JonathanxD/CodeAPI-BytecodeCommon>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.codeapi.bytecodecommon

import com.github.jonathanxd.codeapi.base.MethodDeclaration
import com.github.jonathanxd.codeapi.base.TypeDeclaration
import com.github.jonathanxd.codeapi.common.CodeParameter
import com.github.jonathanxd.codeapi.common.TypeSpec
import com.github.jonathanxd.codeapi.type.CodeType
import com.github.jonathanxd.codeapi.type.GenericType
import com.github.jonathanxd.codeapi.util.CodeTypeUtil as BaseCodeTypeUtil

object CodeTypeUtil {

    fun codeTypeToBinaryName(type: CodeType): String {
        return if (type.isPrimitive)
            primitiveToTypeDesc(type)
        else if (!type.isArray)
            type.type.replace('.', '/')
        else
            toTypeDesc(type)
    }

    fun toTypeDesc(type: CodeType): String = BaseCodeTypeUtil.codeTypeToFullAsm(type)

    fun primitiveToTypeDesc(type: CodeType): String = BaseCodeTypeUtil.primitiveCodeTypeToAsm(type)

    fun codeTypesToBinaryName(type: Iterable<CodeType>): String {
        val sb = StringBuilder()

        for (codeType in type) {
            sb.append(codeTypeToBinaryName(codeType))
        }

        return sb.toString()
    }

    fun codeTypesToTypeDesc(type: Iterable<CodeType>): String {
        val sb = StringBuilder()

        for (codeType in type) {
            sb.append(toTypeDesc(codeType))
        }

        return sb.toString()
    }

    fun typeSpecToTypeDesc(typeSpec: TypeSpec): String {
        return "(" + CodeTypeUtil.codeTypesToTypeDesc(typeSpec.parameterTypes) + ")" +
                CodeTypeUtil.toTypeDesc(typeSpec.returnType)
    }

    fun typeSpecToBinaryName(typeSpec: TypeSpec): String {
        return "(" + CodeTypeUtil.codeTypesToBinaryName(typeSpec.parameterTypes) + ")" +
                CodeTypeUtil.codeTypeToBinaryName(typeSpec.returnType)
    }

    fun toName(codeType: CodeType): String {
        if (codeType is GenericType) {

            val name = codeType.name

            val bounds = codeType.bounds

            if (bounds.isEmpty()) {
                if (!codeType.isType) {
                    if (codeType.isWildcard) {
                        return GenericUtil.fixResult("$name")
                    } else {
                        return GenericUtil.fixResult("T$name;")
                    }
                } else {
                    return name + ";"
                }
            } else {
                return GenericUtil.fixResult(if (!codeType.isWildcard)
                    name + "<" + GenericUtil.bounds(codeType.isWildcard, bounds) + ">;"
                else
                    GenericUtil.bounds(codeType.isWildcard, bounds) + ";")
            }

        } else {
            return GenericUtil.fixResult(toTypeDesc(codeType))
        }
    }

    fun parametersToTypeDesc(codeParameters: Collection<CodeParameter>): String {
        return codeTypesToTypeDesc(codeParameters.map { it.type })
    }

    fun parametersAndReturnToDesc(codeParameters: Collection<CodeParameter>, returnType: CodeType): String {
        return parametersTypeAndReturnToDesc(codeParameters.map { it.type }, returnType)
    }

    fun parametersTypeAndReturnToDesc(parameterTypes: Collection<CodeType>, returnType: CodeType): String {
        return "(${codeTypesToTypeDesc(parameterTypes)})${toTypeDesc(returnType)}"
    }

    /**
     * Infer bound of generic types specified in [method declaration][method] or in [type declaration][owner].
     */
    fun parametersAndReturnToInferredDesc(owner: TypeDeclaration, method: MethodDeclaration, codeParameters: Collection<CodeParameter>, returnType: CodeType): String {

        val genericSign = owner.genericSignature
        val methodGenericSign = method.genericSignature
        val parameterTypes = codeParameters.map { it.type }

        fun infer(codeType: CodeType): CodeType =
                if (codeType is GenericType && !codeType.isType) {
                    GenericUtil.find(methodGenericSign, codeType.name) ?: GenericUtil.find(genericSign, codeType.name) ?: codeType.codeType
                } else {
                    codeType
                }

        return parametersTypeAndReturnToDesc(parameterTypes.map(::infer), infer(returnType))
    }

}