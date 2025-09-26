package net.branchlock.obfuscation.asm

import org.objectweb.asm.*
import org.objectweb.asm.Opcodes.*
import java.lang.reflect.Modifier

class StubVisitor(cv: ClassVisitor) : ClassVisitor(ASM9, cv), Opcodes {
    var access: Int? = null
    var superName: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        this.access = access
        this.superName = superName
        super.visit(version, access, name, null, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        exceptions: Array<String>?
    ): MethodVisitor {
        val visited = cv.visitMethod(access, name, descriptor, null, exceptions)
        if (Modifier.isAbstract(access) || Modifier.isNative(access))
            return visited
        return MethodStubMaker(visited, name, descriptor, access, superName)
    }

    inner class MethodStubMaker(
        mv: MethodVisitor, private val name: String,
        private val desc: String, private val classAccess: Int, superName: String?
    ) :
        MethodVisitor(ASM9, mv) {
        private val superName: String = superName!!

        override fun visitCode() {
            if ("<init>" == name) {
                mv.visitVarInsn(ALOAD, 0)
                var vars = 1
                for (t in Type.getArgumentTypes(desc)) {
                    mv.visitVarInsn(t.getOpcode(ILOAD), vars)
                    vars += t.size
                }
                mv.visitMethodInsn(INVOKESPECIAL, superName, name, desc, Modifier.isInterface(classAccess))
                mv.visitInsn(RETURN)
            } else {
                generateReturn()
            }
        }

        fun generateReturn() {
            val type = Type.getReturnType(desc)
            when (type.sort) {
                Type.OBJECT, Type.ARRAY -> mv.visitInsn(ACONST_NULL)
                Type.BOOLEAN, Type.INT, Type.SHORT, Type.BYTE, Type.CHAR -> mv.visitInsn(ICONST_0)
                Type.LONG -> mv.visitInsn(LCONST_0)
                Type.DOUBLE -> mv.visitInsn(DCONST_0)
                Type.FLOAT -> mv.visitInsn(FCONST_0)
                Type.VOID -> {}
                else -> throw AssertionError()
            }
            mv.visitInsn(type.getOpcode(IRETURN))
            mv.visitEnd()
        }

        override fun visitFrame(type: Int, nLocal: Int, local: Array<Any?>?, nStack: Int, stack: Array<Any?>?) {
            // ignore
        }

        override fun visitInsn(opcode: Int) {
            // ignore
        }

        override fun visitIntInsn(opcode: Int, operand: Int) {
            // ignore
        }

        override fun visitVarInsn(opcode: Int, `var`: Int) {
            // ignore
        }

        override fun visitTypeInsn(opcode: Int, type: String?) {
            // ignore
        }

        override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            // ignore
        }

        override fun visitInvokeDynamicInsn(
            name: String?,
            descriptor: String?,
            bootstrapMethodHandle: Handle?,
            vararg bootstrapMethodArguments: Any?
        ) {
            // ignore
        }

        @Deprecated("Deprecated in Java")
        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
            // ignore
        }

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            // ignore
        }

        override fun visitJumpInsn(opcode: Int, label: Label?) {
            // ignore
        }

        override fun visitLabel(label: Label?) {
            // ignore
        }

        override fun visitLdcInsn(value: Any?) {
            // ignore
        }

        override fun visitIincInsn(`var`: Int, increment: Int) {
            // ignore
        }

        override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
            // ignore
        }

        override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
            // ignore
        }

        override fun visitMultiANewArrayInsn(descriptor: String?, numDimensions: Int) {
            // ignore
        }

        override fun visitTryCatchBlock(start: Label?, end: Label?, handler: Label?, type: String?) {
            // ignore
        }

        override fun visitLocalVariable(
            name: String?,
            descriptor: String?,
            signature: String?,
            start: Label?,
            end: Label?,
            index: Int
        ) {
            // ignore
        }

        override fun visitLineNumber(line: Int, start: Label?) {
            // ignore
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            // ignore
        }

        override fun visitEnd() {
            // ignore
        }
    }
}
