package sample;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import edu.umd.cs.findbugs.BugCollection;
import edu.umd.cs.findbugs.BugInstance;
import edu.umd.cs.findbugs.BugPattern;
import edu.umd.cs.findbugs.BugReporter;
import edu.umd.cs.findbugs.BugReporterObserver;
import edu.umd.cs.findbugs.Detector;
import edu.umd.cs.findbugs.Detector2;
import edu.umd.cs.findbugs.DetectorFactoryCollection;
import edu.umd.cs.findbugs.DetectorToDetector2Adapter;
import edu.umd.cs.findbugs.FindBugs2;
import edu.umd.cs.findbugs.NoOpFindBugsProgress;
import edu.umd.cs.findbugs.Project;
import edu.umd.cs.findbugs.ProjectStats;
import edu.umd.cs.findbugs.ba.AnalysisContext;
import edu.umd.cs.findbugs.classfile.CheckedAnalysisException;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.classfile.Global;
import edu.umd.cs.findbugs.classfile.IAnalysisCache;
import edu.umd.cs.findbugs.classfile.IClassFactory;
import edu.umd.cs.findbugs.classfile.IClassPath;
import edu.umd.cs.findbugs.classfile.IClassPathBuilder;
import edu.umd.cs.findbugs.classfile.ICodeBaseLocator;
import edu.umd.cs.findbugs.classfile.MethodDescriptor;
import edu.umd.cs.findbugs.classfile.impl.ClassFactory;

/**
 * a
 */
public class DetectorDriver {

    private static final String WORK_SPACE_PATH = "";

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        BugReporter bugReporter = new PrintStreamBugReporter(System.out);

        Detector detector1 = new DetectorTutorial(bugReporter);
        Detector detector2 = new LengthCompareDetector(bugReporter);

        BugPattern bugPattern1 = new BugPattern("TUTORIAL_BUG", "TU", "CORRECTNESS", false, "短い説明", "長い説明", "詳細",
                "URL",
                1);
        BugPattern bugPattern2 = new BugPattern("LENGTH_COMPARE", "LC", "CORRECTNESS", false, "短い説明", "長い説明", "詳細",
                "URL",
                1);

        // BigDecimal(double)コンストラクタでインスタンス生成してる箇所検知
        ProjectStats stats = analyze(WORK_SPACE_PATH + "target/test-classes/sample/Target.class",
                detector1, bugPattern1,
                bugReporter);

        // 任意の数値との比較箇所検知（この例では、11,13,16）
        stats = analyze(WORK_SPACE_PATH + "target/test-classes/sample/Target.class",
                detector2, bugPattern2,
                bugReporter);

        System.out.println("バグ数[" + stats.getTotalBugs() + "]");
        stats.reportSummary(System.out);

    }

    /**
     * @param filePath
     * @param detector
     * @param bugPattern
     * @param bugReporter
     * @return
     * @throws CheckedAnalysisException
     * @throws IOException
     * @throws InterruptedException
     */
    private static ProjectStats analyze(String filePath, Detector detector,
            BugPattern bugPattern, BugReporter bugReporter)
            throws CheckedAnalysisException, IOException, InterruptedException {

        // internal to FindBugs, the code uses the Detector2 interface
        Detector2 det = new DetectorToDetector2Adapter(detector);

        // register the rules message
        // I18N.instance().registerBugPattern(bugPattern);
        // FindBugs2.0.1ではこうらしい
        DetectorFactoryCollection.instance().registerBugPattern(bugPattern);

        // a great deal of code to say
        // 'analyze the files in this directory'
        IClassFactory classFactory = ClassFactory.instance();
        IClassPath classPath = classFactory.createClassPath();
        IAnalysisCache analysisCache = classFactory.createAnalysisCache(classPath, bugReporter);
        Global.setAnalysisCacheForCurrentThread(analysisCache);
        FindBugs2.registerBuiltInAnalysisEngines(analysisCache);
        IClassPathBuilder builder = classFactory.createClassPathBuilder(bugReporter);
        ICodeBaseLocator locator = classFactory.createFilesystemCodeBaseLocator(filePath);
        builder.addCodeBase(locator, true);
        builder.build(classPath, new NoOpFindBugsProgress());
        List<ClassDescriptor> classesToAnalyze = builder.getAppClassList();

        // FindBugs3.0.1対応
        // AnalysisCacheToAnalysisContextAdapter analysisContext = new AnalysisCacheToAnalysisContextAdapter();
        // AnalysisContext.setCurrentAnalysisContext(analysisContext);
        Project prj = new Project();
        AnalysisContext aCtx = new AnalysisContext(prj);
        AnalysisContext.setCurrentAnalysisContext(aCtx);

        // finally, perform the analysis
        for (ClassDescriptor d : classesToAnalyze) {
            det.visitClass(d);
        }

        return bugReporter.getProjectStats();
    }

    /**
     * 実行確認用のBugReporter実装クラス（適当）。
     *
     * @author dameningen
     *
     */
    private static class PrintStreamBugReporter implements BugReporter {
        private ProjectStats stats = new ProjectStats();
        private PrintStream os;
        private List<BugReporterObserver> observers = new ArrayList<BugReporterObserver>();

        public PrintStreamBugReporter(PrintStream os) {
            this.os = os;
        }

        @Override
        public void addObserver(BugReporterObserver arg0) {
            observers.add(arg0);
        }

        @Override
        public void finish() {
        }

        @Override
        public ProjectStats getProjectStats() {
            return stats;
        }

        public BugReporter getRealBugReporter() {
            return this;
        }

        @Override
        public void reportBug(BugInstance arg0) {
            stats.addBug(arg0);
            for (BugReporterObserver observer : observers) {
                observer.reportBug(arg0);
            }
            os.println(arg0.getAbridgedMessage());
            os.println("★該当行：" + arg0.getPrimarySourceLineAnnotation().getStartLine());
        }

        @Override
        public void reportQueuedErrors() {
        }

        @Override
        public void setErrorVerbosity(int arg0) {
        }

        @Override
        public void setPriorityThreshold(int arg0) {
        }

        @Override
        public void logError(String arg0) {
            // os.println(arg0.getBytes());
        }

        @Override
        public void logError(String arg0, Throwable arg1) {
            // os.println(arg0.getBytes());
            // arg1.printStackTrace(os);
        }

        @Override
        public void reportMissingClass(ClassNotFoundException arg0) {
            arg0.printStackTrace(os);
        }

        @Override
        public void reportMissingClass(ClassDescriptor arg0) {
            os.println("Class not found: " + arg0);
        }

        @Override
        public void reportSkippedAnalysis(MethodDescriptor arg0) {
            os.println("Skipped Method: " + arg0);
        }

        @Override
        public void observeClass(ClassDescriptor arg0) {
        }

        @Override
        public BugCollection getBugCollection() {
            return null;
        }

    }
}
