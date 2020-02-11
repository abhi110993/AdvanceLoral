
public class SinglyLinkedList {
	
	Node head;
	Node tail;
	int size;
	
	public SinglyLinkedList() {
		size=0;
	}
	
	public void insertAtEnd(Node ele) {
		if(size==0)
			head = tail = ele;
		else {
			tail.next = ele;
			tail = ele;
		}
		size++;
	}
	
	public void insertAtEnd(CascadePath ele) {
		insertAtEnd(new Node(ele));
	}
	
	public void removeFromIndex(int k) {
		Node temp = head;
		for(int i=0;i<=k;i++) {
			if(temp!=null)
				temp = temp.next;
		}
		if(temp!=null)
			temp.next=null;
	}
}
