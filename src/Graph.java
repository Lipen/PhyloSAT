abstract class Graph {
    abstract class Node {
        // TODO: only leaves should have labels... so why does base class has it?
        private String label;


        Node(String label) {
            this.label = label;
        }


        String getLabel() {
            return label;
        }

        void setLabel(String newLabel) {
            this.label = newLabel;
        }

        @Override
        public String toString() {
            return String.format("{%s <%s> @%s}",
                    getClass().getName(),
                    label,
                    Integer.toHexString(hashCode()));
        }
    }
}
