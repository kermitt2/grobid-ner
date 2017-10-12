package org.grobid.trainer;

import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.trainer.assembler.ENAMEXAssembler;
import org.grobid.trainer.assembler.SemDocIdilliaAssembler;
import org.grobid.trainer.assembler.TrainingDataAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Assemble the different corpus information spread in different source files into CoNNL files and TEI files.
 *
 * @author Patrice Lopez
 */
public class CorporaAssembler {

    private static Logger LOGGER = LoggerFactory.getLogger(CorporaAssembler.class);
    private List<TrainingDataAssembler> assemblers = Arrays.asList(
            new ENAMEXAssembler(),
            new SemDocIdilliaAssembler()
    );

    public CorporaAssembler() {}
    
    public CorporaAssembler(List<TrainingDataAssembler> assemblers) {
        this.assemblers = assemblers;
    }

    public void assemble(String outputDirectory) {
        if (isEmpty(assemblers)) {
            throw new GrobidResourceException("No valid assemblers have been specified");
        }

        LOGGER.info("Output directory: " + outputDirectory);
        assemblers.stream().forEach(assembler -> {
            LOGGER.debug("Calling assembler " + assembler.getName());
            assembler.assemble(outputDirectory);
        });
    }

}