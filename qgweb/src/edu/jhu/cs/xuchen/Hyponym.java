/**
 *
 */
package edu.jhu.cs.xuchen;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;

//import wikinet.access.low.API.implement.WikiNet;
//import wikinet.access.low.entry.data.concept.Concept;

/**
 * @author Xuchen Yao
 *
 */
public class Hyponym implements Serializable {

	// ~/wikinet/WikiNet TK Distribution2$ serialver -classpath bin wikinet.access.low.xuchen.Hyponym
	static final long serialVersionUID = 7339302945132689944L;

	protected boolean isNE;
	protected HashSet<String> isASet = new HashSet<String>();
	protected HashSet<String> subOfSet = new HashSet<String>();
	protected String name;
	protected int id;

//	 private void writeObject(java.io.ObjectOutputStream out) throws IOException {
//		 out.writeInt(this.id);
//		 out.writeBoolean(this.isNE);
//		 out.writeObject(this.name);
//		 out.writeObject(this.isASet);
//		 out.writeObject(this.subOfSet);
//	 }
//
//	 private void readObject(java.io.ObjectInputStream in)
//     throws IOException, ClassNotFoundException {
//		 this.id = in.readInt();
//		 this.isNE = in.readBoolean();
//		 this.name = (String) in.readObject();
//		 this.isASet = (HashSet<String>) in.readObject();
//		 this.subOfSet = (HashSet<String>) in.readObject();
//	 }
//	public Hyponym(Concept c, WikiNet acc, Integer id) {
//		this.id = id;
//		this.name = c.getOneCanonicalName("en");
//		this.isNE = c.isNE();
//		if (c.hasRelations()) {
//			for (int rt : c.relations()) {
//				if (acc.getRelationType(rt).getName().equals("IS_A")) {
//					this.isASet = new HashSet<String>();
//					for (int ID : c.getRelationsWith(rt)) {
//						Concept linked = acc.getConcept(ID);
//						this.isASet.add(linked.getOneCanonicalName("en"));
//					}
//				} else if (acc.getRelationType(rt).getName().equals("SUBCAT_OF")) {
//					for (int ID : c.getRelationsWith(rt)) {
//						Concept linked = acc.getConcept(ID);
//						this.subOfSet.add(linked.getOneCanonicalName("en"));
//					}
//				}
//
//			}
//		}
//	}

	public boolean isNE() {return this.isNE;}

	public HashSet<String> getIsASet() {return this.isASet;}

	public HashSet<String> getSubOfSet() {return this.subOfSet;}

	public String getName() {return this.name;}

	public int getID() {return this.id;}

}
