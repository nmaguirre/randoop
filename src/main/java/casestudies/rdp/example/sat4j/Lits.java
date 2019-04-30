package casestudies.rdp.example.sat4j;

import java.io.Serializable;


public final class Lits implements Serializable {

    public static class Dummy {  
    	
    	private static Dummy single_instance = null; 
    	
    	
    	public static Dummy getInstance() {
    		if (single_instance == null) 
                single_instance = new Dummy(); 
      
            return single_instance; 
    		
    	}
    	private Dummy() {
    		
    	}
    	    
    		//public String toString() {
    		//	return "dummy";
    		//}
    }

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_INIT_SIZE = 1;
    //private static int scope = 20;
    
    private boolean pool[] = new boolean[1];

    private int realnVars = 0;

    @SuppressWarnings("unchecked")
    private  Dummy[] watches = new Dummy[0];

    private  int[] level = new int[0];

    private Dummy[] reason = new Dummy[0];

    private int maxvarid = 0;

    @SuppressWarnings("unchecked")
    private Dummy[] undos = new Dummy[0];

    private  boolean[] falsified = new boolean[0];

    
    public Lits() {
        init(DEFAULT_INIT_SIZE);
    }

    @SuppressWarnings({ "unchecked" })
     private  void init(int nvar) {
        if (nvar < this.pool.length) {
            return;
        }
        // let some space for unused 0 indexer.
        int nvars = nvar + 1;
        boolean[] npool = new boolean[nvars];
        System.arraycopy(this.pool, 0, npool, 0, this.pool.length);
        this.pool = npool;

        int[] nlevel = new int[nvars];
        System.arraycopy(this.level, 0, nlevel, 0, this.level.length);
        this.level = nlevel;

        Dummy[] nwatches = new Dummy[2 * nvars];
        System.arraycopy(this.watches, 0, nwatches, 0, this.watches.length);
        this.watches = nwatches;

        Dummy[] nundos = new Dummy[nvars];
        System.arraycopy(this.undos, 0, nundos, 0, this.undos.length);
        this.undos = nundos;

        Dummy[] nreason = new Dummy[nvars];
        System.arraycopy(this.reason, 0, nreason, 0, this.reason.length);
        this.reason = nreason;

        boolean[] newFalsified = new boolean[2 * nvars];
        System.arraycopy(this.falsified, 0, newFalsified, 0,
                this.falsified.length);
        this.falsified = newFalsified;
    }

    public int getFromPool(int x) {
        int var = Math.abs(x);
        if (var >= this.pool.length) {
            init(Math.max(var, this.pool.length << 1));
        }
        //assert var < this.pool.length;
        if (var >= this.pool.length) throw new IllegalArgumentException();
        
        if (var > this.maxvarid) {
            this.maxvarid = var;
        }
        int lit = LiteralsUtils.toInternal(x);
        //assert lit > 1;
        if (lit <= 1) throw new IllegalArgumentException();
        
        if (!this.pool[var]) {
        	// pool[var] is false
            this.realnVars++;
            this.pool[var] = true;
            //this.watches[var << 1] = new Dummy();
            this.watches[var << 1] = Dummy.getInstance();
            //this.watches[var << 1 | 1] = new Dummy();
            this.watches[var << 1 | 1] = Dummy.getInstance();
            //this.undos[var] = new Dummy();
            this.undos[var] = Dummy.getInstance();
            this.level[var] = -1;
            this.falsified[var << 1] = false; // because truthValue[var] is
            // UNDEFINED
            this.falsified[var << 1 | 1] = false; // because truthValue[var] is
            // UNDEFINED
        }
        return lit;
    }

    public boolean belongsToPool(int x) {
        //assert x > 0;
       if (x<=0) throw new IllegalArgumentException();
        
    	if (x >= this.pool.length) {
            return false;
        }
        return this.pool[x];
    }

    public void resetPool() {
        for (int i = 0; i < this.pool.length; i++) {
            if (this.pool[i]) {
                reset(i << 1);
            }
        }
        this.maxvarid = 0;
        this.realnVars = 0;
    }

    public void ensurePool(int howmany) {
        if (howmany >= this.pool.length) {
            init(Math.max(howmany, this.pool.length << 1));
        }
        if (this.maxvarid < howmany) {
            this.maxvarid = howmany;
        }
    }

    public void unassign(int lit) {
    //	assert this.falsified[lit] || this.falsified[lit ^ 1];
    	if(!(this.falsified[lit] || this.falsified[lit ^ 1]))
    		throw new IllegalArgumentException();
    	/*added*/
     if (lit < 0 || lit >= this.falsified.length || (!this.falsified[lit] && !this.falsified[lit^1]))
        	throw new IllegalArgumentException();
    	/******/
     this.falsified[lit] = false;
     this.falsified[lit ^ 1] = false;
    }

    public void satisfies(int lit) {
       // assert !this.falsified[lit] && !this.falsified[lit ^ 1];
    	  if(!(!this.falsified[lit] && !this.falsified[lit ^ 1]))
    		  throw new IllegalArgumentException();
    	 /*added*/
       if (lit<0 || lit>=this.falsified.length||(this.falsified[lit]||this.falsified[lit ^ 1]))
          throw new IllegalArgumentException();
    	 /******/ 
    	  this.falsified[lit] = false;
        this.falsified[lit ^ 1] = true;
    }

    public void forgets(int var) {
   	 /*added*/
    		if ((var << 1)< 0 || (var << 1) >=this.falsified.length)
    			throw new IllegalArgumentException();
    		if ((var << 1 ^ 1)<0 || (var << 1 ^ 1)>=this.falsified.length)
    			throw new IllegalArgumentException();
   	 /******/ 
        this.falsified[var << 1] = true;
        this.falsified[var << 1 ^ 1] = true;
    }

    public boolean isSatisfied(int lit) {
    	 	/*added*/
    		if ((lit ^ 1)<0 || (lit ^ 1)>=this.falsified.length)
    			throw new IllegalArgumentException();
    		/******/     
    		return this.falsified[lit ^ 1];
        
    }

    public boolean isFalsified(int lit) {
   	/*added*/
    	if (lit < 0 || lit >= this.falsified.length)
    		throw new IllegalArgumentException();
    	/******/ 
        return this.falsified[lit];
    }

    public boolean isUnassigned(int lit) {
    	/*added*/
    	if (lit<0 || lit>=this.falsified.length)
    		throw new IllegalArgumentException();
    	if ((lit ^ 1)<0 || (lit ^ 1)>=falsified.length)
    		throw new IllegalArgumentException();
    	/******/
        return !this.falsified[lit] && !this.falsified[lit ^ 1];
    }

    public String valueToString(int lit) {
        if (isUnassigned(lit)) {
            return "?"; //$NON-NLS-1$
        }
        if (isSatisfied(lit)) {
            return "T"; //$NON-NLS-1$
        }
        return "F"; //$NON-NLS-1$
    }

    public int nVars() {
        // return pool.length - 1;
        return this.maxvarid;
    }

    public int not(int lit) {
        return lit ^ 1;
    }

    public static String toString(int lit) {
        return ((lit & 1) == 0 ? "" : "-") + (lit >> 1); //$NON-NLS-1$//$NON-NLS-2$
    }

    /* public static String toStringX(int lit) {
        return ((lit & 1) == 0 ? "+" : "-") + "x" + (lit >> 1); //$NON-NLS-1$//$NON-NLS-2$
    }*/

    public void reset(int lit) {
    		//this.watches[lit].clear();
    		/*added*/
      
    		if(this.watches[lit]==null)
    			throw new NullPointerException();
    		/*********/
    	   // this.watches[lit] = new Dummy();
    		 this.watches[lit] = Dummy.getInstance();
    	    //this.watches[lit ^ 1].clear();
    	    /*added*/
    	    if(this.watches[lit ^ 1]==null)
    	    		throw new NullPointerException();
    	    /*********/
    	    //this.watches[lit ^ 1] = new Dummy();
    	    this.watches[lit ^ 1] = Dummy.getInstance();
        this.level[lit >> 1] = -1;
        this.reason[lit >> 1] = null;
        
        //this.undos[lit >> 1].clear();
        /*added*/
        if (this.undos[lit >> 1]==null)
        		throw new NullPointerException();
        /*********/
       // this.undos[lit >> 1] = new Dummy();
        this.undos[lit >> 1] = Dummy.getInstance();
        
        this.falsified[lit] = false;
        this.falsified[lit ^ 1] = false;
        this.pool[lit >> 1] = false;
    }

    public int getLevel(int lit) {    		
        return this.level[lit >> 1];
    }

    public void setLevel(int lit, int l) {
    		/*added*/
    		if ((lit >> 1)<0 || (lit >> 1)>=this.level.length)
    			throw new IllegalArgumentException();
    		 /*********/
        this.level[lit >> 1] = l;
    }

    public Object getReason(int lit) {
        return this.reason[lit >> 1];
    }

   // public void setReason(int lit, Dummy r) {
    public void setReason(int lit) {
    		/***added***/
    		if ((lit >> 1)<0 || (lit >> 1)>=this.reason.length)
    			throw new IllegalArgumentException();
    		/*********/
        this.reason[lit >> 1] = Dummy.getInstance();
    }
    
    public Object undos(int lit) {
        return this.undos[lit >> 1];
    }

//    public void watch(int lit, Object c) {
    //	if(this.watches[lit]==null)
    	//	throw new NullPointerException();
//        this.watches[lit].push(c);
  //  }

    public Object watches(int lit) {
        return this.watches[lit];
    }

    public boolean isImplied(int lit) {
        int var = lit >> 1;
        //assert this.reason[var] == null || this.falsified[lit]
                //|| this.falsified[lit ^ 1];
        if(!(this.reason[var] == null || this.falsified[lit]
                || this.falsified[lit ^ 1]))
        		throw new IllegalArgumentException();  
        // a literal is implied if it is a unit clause, ie
        // propagated without reason at decision level 0.
        return this.pool[var]
                && (this.reason[var] != null || this.level[var] == 0);
    }

    public int realnVars() {
        return this.realnVars;
    }

    /**
     * To get the capacity of the current vocabulary.
     * 
     * @return the total number of variables that can be managed by the
     *         vocabulary.
     */
    protected int capacity() {
        return this.pool.length - 1;
    }

    /**
     * @since 2.1
     */
    public int nextFreeVarId(boolean reserve) {
        if (reserve) {
            ensurePool(this.maxvarid + 1);
            // ensure pool changes maxvarid
            return this.maxvarid;
        }
        return this.maxvarid + 1;
    }
   
}

