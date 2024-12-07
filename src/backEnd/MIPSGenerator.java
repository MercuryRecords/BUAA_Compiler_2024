package backEnd;

import middleEnd.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

public class MIPSGenerator {
    private final LLVMModule llvmModule;
    public MIPSGenerator(LLVMModule llvmModule) {
        this.llvmModule = llvmModule;
    }

    public void translate(String forOutput) {
        MIPSDataSection mipsDataSection = translateMIPSDataSection();
        MIPSTextSection mipsTextSection = translateMIPSTextSection();
        try (FileWriter writer = new FileWriter(forOutput)) {
            writer.write(mipsDataSection.toString());
            writer.write(mipsTextSection.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private MIPSTextSection translateMIPSTextSection() {
        MIPSTextSection newSection = new MIPSTextSection();
        for (LLVMFunction LLVMFunction : llvmModule.LLVMFunctions) {
            newSection.addFunction(translateMIPSFunction(LLVMFunction));
        }
        return newSection;
    }

    private MIPSFunction translateMIPSFunction(LLVMFunction llvmFunction) {
        MIPSFunction newFunction = new MIPSFunction(llvmFunction.name);
        MIPSManager.getInstance().setCurrentFunction(newFunction);
        for (LLVMBasicBlock basicBlock : llvmFunction.basicBlocks) {
            newFunction.addBasicBlock(translateMIPSBasicBlock(basicBlock));
        }
        return newFunction;
    }

    private MIPSBasicBlock translateMIPSBasicBlock(LLVMBasicBlock basicBlock) {
        MIPSBasicBlock newBlock = new MIPSBasicBlock();
        for (LLVMInstruction instruction : basicBlock.instructions) {
            newBlock.addInstructions(translateMIPSInstruction(instruction));
        }
        return newBlock;
    }

    private LinkedList<MIPSInst> translateMIPSInstruction(LLVMInstruction instruction) {
        return instruction.toMIPS();
    }

    private MIPSDataSection translateMIPSDataSection() {
        MIPSDataSection newSection = new MIPSDataSection();
        for (Value value : llvmModule.globalValues) {
            if (value instanceof GlobalVariable) {
                newSection.addGlobalVariable((GlobalVariable) value);
            } else {
                newSection.addString((GlobalString) value);
            }
        }
        return newSection;
    }
}
