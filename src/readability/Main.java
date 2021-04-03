package readability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

        String fileName = args[0];

        String fileContent = "";
        try {
            fileContent = Files.readString(Path.of(fileName));
        } catch (IOException e) {
            System.out.println("No such file");
            System.out.println(e.getMessage());
        }

        TextStats stats = new TextStats(fileContent);
        System.out.println(stats);
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        Scanner scanner = new Scanner(System.in);
        String choice = scanner.next();

        switch (choice) {
            case "ARI":
                stats.printARIScore();
                break;
            case "FK":
                stats.printFKScore();
                break;
            case "SMOG":
                stats.printSMOGScore();
                break;
            case "CL":
                stats.printCLScore();
                break;
            case "all":
                stats.printAllScores();
                break;
            default:
                break;
        }
    }
}

class TextStats {
    private long nCharacters;
    private long nWords;
    private long nSentences;
    private long nSyllables;
    private long nPolysyllables;

    private final String content;


    TextStats(String content) {
        this.content = content;
        analyzeContent();
    }

    private void analyzeContent() {

        // count sentences
        Pattern sentencePattern = Pattern.compile("\\s*\\w+[\\w\\s]*([.?!]|$)");
        Matcher sentenceMatcher = sentencePattern.matcher(this.content);
        this.nSentences = sentenceMatcher.results().count();

        // count words
        Pattern wordPattern = Pattern.compile("\\b\\S+\\b");
        Matcher wordMatcher = wordPattern.matcher(this.content);
        this.nWords = wordMatcher.results().count();

        // count characters
        Pattern characterPattern = Pattern.compile("\\S");
        Matcher characterMatcher = characterPattern.matcher(this.content);
        this.nCharacters = characterMatcher.results().count();

        // count syllables and polysyllables
        this.nSyllables = 0;
        this.nPolysyllables = 0;
        Pattern vowelsPattern = Pattern.compile("[aeiouyAEIOUY]+");
        Pattern letterWordPattern = Pattern.compile("\\b\\S+\\b");
        Matcher letterWordMatcher = letterWordPattern.matcher(this.content);
        while (letterWordMatcher.find()) {
            String word = letterWordMatcher.group();
            if (word.matches(".*[eE]$")) {
                word = word.substring(0, word.length() - 1);
            }
            Matcher vowelsMatcher = vowelsPattern.matcher(word);
            long vowels = vowelsMatcher.results().count();
            long syllables = (vowels == 0) ? 1 : vowels;
            this.nSyllables += syllables;
            if (syllables > 2) {
                this.nPolysyllables += 1;
            }
        }
    }

    public double computeColemanLiauScore() {
        return 0.0588 * (this.nCharacters / (this.nWords / 100.0)) - 0.296 * (this.nSentences / (this.nWords / 100.0)) - 15.8;
    }

    public double computeFleschKincaidScore() {
        return 0.39 * this.nWords / this.nSentences + 11.8 * this.nSyllables / this.nWords - 15.59;
    }

    public double computeSMOGscore() {
        return 1.043 * Math.sqrt(this.nPolysyllables * 30.0 / this.nSentences) + 3.1291;
    }

    public double computeARIscore() {
        return 4.71 * this.nCharacters / this.nWords + 0.5 * this.nWords / this.nSentences - 21.43;
    }

    public static int computeAgeGroup(double score) {
        int group = (int) Math.round(score);
        switch (group) {
            case 1: return 6;
            case 2: return 7;
            case 3: return 9;
            case 4: return 10;
            case 5: return 11;
            case 6: return 12;
            case 7: return 13;
            case 8: return 14;
            case 9: return 15;
            case 10: return 16;
            case 11: return 17;
            case 12: return 18;
            //case 13: return 24";
            default: return 24;
        }
    }

    public String toString() {
        return "The text is:\n" +
                this.content + "\n\n" +
                "Words: " + this.nWords + "\n" +
                "Sentences: " + this.nSentences + "\n" +
                "Characters: " + this.nCharacters + "\n" +
                "Syllables: " + this.nSyllables + "\n" +
                "Polysyllables: " + this.nPolysyllables + "\n";
    }

    public void printARIScore() {
        double score = this.computeARIscore();
        int ageGroup = TextStats.computeAgeGroup(score);
        printTestStats("Automated Readability Index", score, ageGroup);
    }

    public void printFKScore() {
        double score = this.computeFleschKincaidScore();
        int ageGroup = TextStats.computeAgeGroup(score);
        printTestStats("Flesch–Kincaid readability tests", score, ageGroup);
    }

    public void printSMOGScore() {
        double score = this.computeSMOGscore();
        int ageGroup = TextStats.computeAgeGroup(score);
        printTestStats("Simple Measure of Gobbledygook", score, ageGroup);
    }

    public void printCLScore() {
        double score = this.computeColemanLiauScore();
        int ageGroup = TextStats.computeAgeGroup(score);
        printTestStats("Coleman–Liau index", score, ageGroup);
    }

    public void printAllScores() {
        double ARIscore = this.computeARIscore();
        int ARIAgeGroup = TextStats.computeAgeGroup(ARIscore);

        double FKscore = this.computeFleschKincaidScore();
        int FKAgeGroup = TextStats.computeAgeGroup(FKscore);

        double SMOGscore = this.computeSMOGscore();
        int SMOGAgeGroup = TextStats.computeAgeGroup(SMOGscore);

        double CLscore = this.computeColemanLiauScore();
        int CLAgeGroup = TextStats.computeAgeGroup(CLscore);

        printTestStats("\nAutomated Readability Index", ARIscore, ARIAgeGroup);
        printTestStats("Flesch–Kincaid readability tests", FKscore, FKAgeGroup);
        printTestStats("Simple Measure of Gobbledygook", SMOGscore, SMOGAgeGroup);
        printTestStats("Coleman–Liau index", CLscore, CLAgeGroup);

        double averageAgeGroup = (ARIAgeGroup + FKAgeGroup + SMOGAgeGroup + CLAgeGroup) / 4.0;
        System.out.printf("\nThis text should be understood in average by %.2f-year-olds.\n", averageAgeGroup);
    }

    private void printTestStats(String testName, double score, int ageGroup) {
        System.out.printf("%s: %.2f ", testName, score);
        System.out.printf("(about %d-year-olds).\n", ageGroup);
    }
}

