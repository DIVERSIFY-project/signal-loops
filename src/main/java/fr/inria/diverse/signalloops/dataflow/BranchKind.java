package fr.inria.diverse.signalloops.dataflow;

/**
 * Branching for a node
 */
public enum BranchKind {
    BRANCH,      // Represents a branch
    STATEMENT,   // Represents an statement
    BLOCK_BEGIN, // Represents the begining of a block
    BLOCK_END,   // Represents the end of a block
    CONVERGE     // A temporary convergence node. The nodes being mark as 'CONVERGE' are temporal an will be deleted
                 // eventually.
}