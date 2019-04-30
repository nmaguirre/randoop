package randoop.test.datastructures.binheap;


public class NodeWrapper {
    BinomialHeapNode node;

    NodeWrapper(BinomialHeapNode n) {
        this.node = n;
    }

    public boolean equals(Object o) {
        if (!(o instanceof NodeWrapper))
            return false;
        return node == ((NodeWrapper) o).node;
    }

    public int hashCode() {
        return System.identityHashCode(node);
    }
}