package org.grobid.trainer.assembler;

public interface TrainingDataAssembler {

    void assemble(String outputDirectory);

    String getName();
}
