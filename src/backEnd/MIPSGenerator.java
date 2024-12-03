package backEnd;

import middleEnd.LLVMModule;

import java.io.FileWriter;
import java.io.IOException;

public class MIPSGenerator {
    private final LLVMModule llvmModule;
    public MIPSGenerator(LLVMModule llvmModule) {
        this.llvmModule = llvmModule;
    }

    public void translate(String forOutput) {
        MIPSDataSection mipsDataSection = llvmModule.translateToMIPSDataSection();
        try (FileWriter writer = new FileWriter(forOutput)) {
            writer.write(mipsDataSection.toString());
            // writer.write(module.toString());
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}
