
//
// Creator:    http://www.dicelocksecurity.com
// Version:    vers.7.0.0.1
//
// Copyright (C) 2011-2012 DiceLock Security, LLC. All rights reserved.
//
//                               DISCLAIMER
//
// THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES,
// INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
// AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
// SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
// DICELOCK IS A REGISTERED TRADEMARK OR TRADEMARK OF THE OWNERS.
//
// Environment:
// java version "1.6.0_29"
// Java(TM) SE Runtime Environment (build 1.6.0_29-b11)
// Java HotSpot(TM) Server VM (build 20.4-b02, mixed mode)
//
 
package com.dicelocksecurity.jdicelock.RandomTest;
 
import com.dicelocksecurity.jdicelock.CryptoRandomStream.BaseCryptoRandomStream;
 
/**
 * Base class of all random number tests implemented
 *
 * @author      Angel Ferré @ DiceLock Security
 * @version     7.0.0.1
 * @since       2011-09-29
 */
public abstract class BaseRandomTest {
 
    /**
     * "alpha" parameter setting confidence level
     */
    protected double alpha;
 
    /**
     * "pValue" result to verify randomness properties
     */
    protected double pValue;
 
    /**
     * "random" result
     * true:    if last checked stream was a randomized stream
     * false:   if last checked stream was not a randomized stream
     */
    protected boolean random;
 
    /**
     * "error"    RandomTestErrors enumeration value indicating the error (if has been produced) of last stream checked
     */
    protected RandomTestErrors error;
 
    /**
     * "MathematicalFunctions" object that is being used by random number test
     */
    protected MathematicalFunctions mathFuncs;
 
    /**
     * boolean indicating if "mathFuncs" object has been internally created
     * true:    if "mathFuncs" object has been instantiated within random number test object
     * false:   if "mathFuncs" object has been instantiated outside random number test object
     */
    protected boolean autoMathFunc;
 
    /**
     * Constructor, default
     */
    public BaseRandomTest() {
 
        super();
 
        this.error = RandomTestErrors.NoError;
        this.alpha = 0.0;
        this.pValue = 0.0;
        this.mathFuncs = new MathematicalFunctions();
        if (this.mathFuncs == null) {
            this.error = RandomTestErrors.MathematicalFunctionsError;
            this.autoMathFunc = false;
        } else {
            this.autoMathFunc = true;
        }
        this.random = false;
    }
 
    /**
     * Constructor with a MathematicalFunctions object instantiated
     */
    public BaseRandomTest(MathematicalFunctions mathFuncObj) {
 
        this.error = RandomTestErrors.NoError;
        if (mathFuncObj != null) {
            this.mathFuncs = mathFuncObj;
            this.autoMathFunc = false;
        } else {
            this.mathFuncs = new MathematicalFunctions();
            if (this.mathFuncs == null) {
                this.error = RandomTestErrors.MathematicalFunctionsError;
                this.autoMathFunc = false;
            } else {
                this.autoMathFunc = true;
            }
        }
        this.alpha = 0.0;
        this.pValue = 0.0;
        this.random = false;
    }
 
    /**
     * Destructor
     */
    public void finalize() {
 
        this.alpha = 0.0;
        this.pValue = 0.0;
        if ((this.autoMathFunc) && (this.mathFuncs != null)) {
            this.mathFuncs = null;
        }
        this.mathFuncs = null;
        this.random = false;
        this.error = RandomTestErrors.NoError;
    }
 
    /**
     * Sets the BaseRandomTest alpha margin
     */
    public void SetAlpha(double newAlpha) {
 
        this.alpha = newAlpha;
    }
 
    /**
     * Gets the BaseRandomTest alpha margin
     */
    public double GetAlpha() {
 
        return this.alpha;
    }
 
    /**
     * Gets the BaseRandomTest pValue
     */
    public double GetPValue() {
 
        return this.pValue;
    }
 
    /**
     * Gets the BaseRandomTest error of the last executed BaseCryptoRandomStream
     */
    public RandomTestErrors GetError() {
 
        return this.error;
    }
 
    /**
     * Gets the BaseRandomTest random state of the last executed BaseCryptoRandomStream
     */
    public boolean IsRandom() {
 
        return this.random;
    }
 
    /**
     * Tests the BaseCryptoRandomStream executed and returns the random value
     */
    abstract public boolean IsRandom(BaseCryptoRandomStream stream);
 
    /**
     * Initialize the object
     */
    public void Initialize() {
 
        this.pValue = 0.0;
        this.random = false;
        this.error = RandomTestErrors.NoError;
    }
 
    /**
     * Gets the type of the object
     */
    abstract public RandomTests GetType();
 
    /**
     * Gets the minimum stream length
     */
    abstract public int GetMinimumLength();
 
}
 