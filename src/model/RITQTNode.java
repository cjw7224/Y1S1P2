package model;

/**
 * Represents a Quadtree node in the tree for an image compressed using the
 * Rich Image Tool file format.
 *
 * A node contains a value which is either a grayscale color (0-255) for a
 * region, or QTree.QUAD_SPLIT meaning this node cannot hold a single color
 * and thus has split itself into 4 sub-regions.
 *
 * @author Sean Strout @ RIT
 */
public class RITQTNode {
	/** The node's value */
	private int val;

	/** quadrant II */
	private RITQTNode ul;

	/** quadrant I */
	private RITQTNode ur;

	/** quadrant III */
	private RITQTNode ll;

	/** quadrant IV */
	private RITQTNode lr;

	/**
	 * Construct a leaf node with no children.
	 * 
	 * @param val node value
	 */
	public RITQTNode(int val) {
		this(val, null, null, null, null);
	}

	/**
	 * Construct a quad tree node.
	 *
	 * @param val the node's value
	 * @param ul  the upper left sub-node
	 * @param ur  the upper right sub-node
	 * @param ll  the lower left sub-node
	 * @param lr  the lower right sub-node
	 */
	public RITQTNode(int val, RITQTNode ul, RITQTNode ur, RITQTNode ll, RITQTNode lr) {
		this.val = val;
		this.ul = ul;
		this.ur = ur;
		this.ll = ll;
		this.lr = lr;
	}

	/**
	 * Get the node's value.
	 *
	 * @return node's value
	 */
	public int getVal() {
		return this.val;
	}

	/**
	 * Get the upper left sub-node.
	 *
	 * @return upper left sub-node
	 */
	public RITQTNode getUpperLeft() {
		return this.ul;
	}

	/**
	 * Get the upper right sub-node.
	 *
	 * @return upper right sub-node
	 */
	public RITQTNode getUpperRight() {
		return this.ur;
	}

	/**
	 * Get the lower left sub-node.
	 *
	 * @return lower left sub-node
	 */
	public RITQTNode getLowerLeft() {
		return this.ll;
	}

	/**
	 * Get the lower right sub-node
	 *
	 * @return lower right sub-node
	 */
	public RITQTNode getLowerRight() {
		return this.lr;
	}

	/**
	 * Returns whether this particular node is a leaf node (i.e. it has no children)
	 * 
	 * @return true if this is a leaf node, false if not.
	 */
	public boolean isLeaf() {
		return val != -1;
	}

	@Override
	public String toString() {
		if (ul != null) {
			return (" ( " + ul.toString() + " " + ur.toString() + " " + ll.toString() + " " + lr.toString() + " ) ")
					.replace("  ", " ");
		} else {
			return val + "";
		}
	}
}
