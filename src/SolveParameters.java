class SolveParameters {
    int hybridizationNumber;
    int maxChildren;
    int maxParents;
    long firstTimeLimit;
    long maxTimeLimit;
    int checkFirst;
    String prefix;
    boolean isExternal;

    SolveParameters(int hybridizationNumber, int maxChildren, int maxParents, long firstTimeLimit, long maxTimeLimit, int checkFirst, String prefix, boolean isExternal) {
        this.hybridizationNumber = hybridizationNumber;
        this.maxChildren = maxChildren;
        this.maxParents = maxParents;
        this.firstTimeLimit = firstTimeLimit;
        this.maxTimeLimit = maxTimeLimit;
        this.checkFirst = checkFirst;
        this.prefix = prefix;
        this.isExternal = isExternal;
    }
}
