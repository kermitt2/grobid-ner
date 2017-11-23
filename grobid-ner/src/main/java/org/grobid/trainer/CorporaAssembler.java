package org.grobid.trainer;

import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.trainer.assembler.ENAMEXAssembler;
import org.grobid.trainer.assembler.SemDocIdilliaAssembler;
import org.grobid.trainer.assembler.TrainingDataAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Assemble the different corpus information spread in different source files into CoNNL files and TEI files.
 *
 * @author Patrice Lopez
 */
public class CorporaAssembler {

    public static final String IDILLIA = "idillia";
    private static Logger LOGGER = LoggerFactory.getLogger(CorporaAssembler.class);

    private Map<String, TrainingDataAssembler> assemblers = Collections.unmodifiableMap(
            Stream.of(
                    new SimpleEntry<>(IDILLIA, new SemDocIdilliaAssembler())
            ).collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));

    public CorporaAssembler() {
    }

    public CorporaAssembler(Map<String, TrainingDataAssembler> assemblers) {
        this.assemblers = assemblers;
    }

    public void assemble(String assembler, String outputDirectory) {
        if (isEmpty(assemblers.keySet())) {
            throw new GrobidResourceException("No valid assemblers have been specified");
        }

        LOGGER.info("Output directory: " + outputDirectory);
        assemblers.get(assembler).assemble(outputDirectory);
    }

}