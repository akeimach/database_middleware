class RunThreads implements Runnable {

	
	private Thread thread;
	private String threadName;

	
	RunThreads(String name) {
		threadName = name;
		System.out.println("Creating " +  threadName );
	}

	public void run() {
		System.out.println("Running " +  threadName );
		try {
			for (int i = 4; i > 0; i--) {
				System.out.println("Thread: " + threadName + ", " + i);
				Thread.sleep(50); //Let the thread sleep
			}
		} 
		catch (InterruptedException e) { System.out.println("Thread " +  threadName + " interrupted."); }
		System.out.println("Thread " +  threadName + " exiting.");
	}

	
	public void start() {
		System.out.println("Starting " +  threadName );
		if (thread == null) {
			thread = new Thread (this, threadName);
			thread.start ();
		}
	}

	public static void main(String args[]) {

		RunThreads R1 = new RunThreads("Thread-1");
		R1.start();

		RunThreads R2 = new RunThreads("Thread-2");
		R2.start();
	}   
}
