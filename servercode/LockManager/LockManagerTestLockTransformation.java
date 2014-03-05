package LockManager;
import LockManager.*;

class LockManagerTestLockTransformation {
	public static void main (String[] args) {
		MyT t1, t2;
		LockManager lm = new LockManager ();
		t1 = new MyT (lm, 1);
		t2 = new MyT (lm, 2);
		t1.start ();
		t2.start ();
	}
}

class MyT extends Thread {
	LockManager lm;
	int threadId;

	public MyT (LockManager lm, int threadId) {
		this.lm = lm;
		this.threadId = threadId;
	}

	public void run () {
		if (threadId == 1) {
			try {
				lm.Lock (1, "a", LockManager.READ);
			}
			catch (DeadlockException e) {
				System.out.println ("Deadlock.... ");
			}

			try {
				this.sleep (4000);
			}
			catch (InterruptedException e) { }

			try {

				lm.Lock (1, "b", LockManager.READ);
			}
			catch (DeadlockException e) {
				System.out.println ("Deadlock.... not ");
			}
			try {

				lm.Lock (1, "b", LockManager.READ);
			}
			catch (DeadlockException e) {
				System.out.println ("Deadlock.... ");
			}

			lm.UnlockAll (1);
		}
		else if (threadId == 2) {
			try {
				lm.Lock (2, "b", LockManager.READ);
			}
			catch (DeadlockException e) { 
				System.out.println ("Deadlock.... ");
			}

			try {
				this.sleep (1000);
			}
			catch (InterruptedException e) { }

			try {
				lm.Lock (2, "a", LockManager.WRITE);
			}
			catch (DeadlockException e) { 
				System.out.println ("Deadlock.... ");
			}

			lm.UnlockAll (2);
		}
	}
}
