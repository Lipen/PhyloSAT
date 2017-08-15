class SolveParameters {
    int hybridizationNumber;
    int maxChildren;
    int maxParents;
    long firstTimeLimit;
    long maxTimeLimit;
    int checkFirst;
    String prefix;
    boolean isExternal;
    int threads;
    boolean isDumping;
    String basefilename;
    int numberOfSolutions;

    SolveParameters(int hybridizationNumber, int maxChildren, int maxParents, long firstTimeLimit, long maxTimeLimit, int checkFirst, String prefix, boolean isExternal, int threads, boolean isDumping, String basefilename, int numberOfSolutions) {
        this.hybridizationNumber = hybridizationNumber;
        this.maxChildren = maxChildren;
        this.maxParents = maxParents;
        this.firstTimeLimit = firstTimeLimit;
        this.maxTimeLimit = maxTimeLimit;
        this.checkFirst = checkFirst;
        this.prefix = prefix;
        this.isExternal = isExternal;
        this.threads = threads;
        this.isDumping = isDumping;
        this.basefilename = basefilename;
        this.numberOfSolutions = numberOfSolutions;
    }
}
