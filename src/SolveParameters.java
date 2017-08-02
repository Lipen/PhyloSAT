class SolveParameters {
    int hybridizationNumber;
    int maxChildren;
    int maxParents;
    long firstTimeLimit;
    long maxTimeLimit;
    int checkFirst;

    SolveParameters(int hybridizationNumber, int maxChildren, int maxParents, long firstTimeLimit, long maxTimeLimit, int checkFirst) {
        this.hybridizationNumber = hybridizationNumber;
        this.maxChildren = maxChildren;
        this.maxParents = maxParents;
        this.firstTimeLimit = firstTimeLimit;
        this.maxTimeLimit = maxTimeLimit;
        this.checkFirst = checkFirst;
    }
}
