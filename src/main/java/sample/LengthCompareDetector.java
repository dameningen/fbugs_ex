package sample;

import org.apache.bcel.classfile.Code;

import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BytecodeScanningDetector;

/**
 * 
 */
public class LengthCompareDetector extends BytecodeScanningDetector {

    private BugReporter bugReporter;

    private int binpushPC = 0;

    private int numCompareBlockStartPC = 0;

    private boolean DEBUG = true;

    public LengthCompareDetector(BugReporter bugReporter) {
        this.bugReporter = bugReporter;
    }

    @Override
    public void visit(Code code) {
        binpushPC = Integer.MIN_VALUE;
        numCompareBlockStartPC = 0;
        super.visit(code);
    }

    @Override
    public void sawOpcode(int seen) {
        if (DEBUG) {
            // System.out.println(getCode().toString());
            System.out.println(getPC() + ":" + seen);
            printOpCode(seen);
        }

        // bipush
        if (seen == BIPUSH) {
            int value = getIntConstant();
            System.out.println("value[" + value + "]");
            if (value == 11 || value == 13 || value == 16) {
                binpushPC = getPC();
                System.out.println("■binpushPC:" + binpushPC);
                return;
            }
        }

        // if (seen == INVOKEVIRTUAL) {
        // int value = 1;
        // System.out.println("[INVOKEVIRTUAL]value[" + value + "]");
        // if (value == 11 || value == 13 || value == 16) {
        // binpushPC = getPC();
        // System.out.println("■[INVOKEVIRTUAL]binpushPC:" + binpushPC);
        // return;
        // }
        // }

        if (seen == IF_ICMPEQ || seen == IF_ICMPGE || seen == IF_ICMPGT
                || seen == IF_ICMPLE || seen == IF_ICMPLT || seen == IF_ICMPNE) {
            if (getPC() >= binpushPC + 2 && getPC() < binpushPC + 7) {
                bugReporter.reportBug(new BugInstance(this, "LENGTH_COMPARE",
                        NORMAL_PRIORITY).addClassAndMethod(this)
                        .addString("固定長比較？").addSourceLine(this));
            }

        }

        // if (getPrevOpcode(1) == BIPUSH) {
        // // System.out.println(getNextOpcode());
        // // System.out.println(getCode().toString());
        // System.out.println("a");
        // }
    }
}