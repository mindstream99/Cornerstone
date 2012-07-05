/* Generated By:JavaCC: Do not edit this line. CSLRuleParserConstants.java */
/**
 * If you're going to extend the language, you'll need to generate your own parser.
 * So copy this file into your own package directory, and change the package specification
 * below to your own package.
 */
package com.paxxis.cornerstone.scripting.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CSLRuleParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int WHITESPACE = 1;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 4;
  /** RegularExpression Id. */
  int MULTI_LINE_COMMENT = 5;
  /** RegularExpression Id. */
  int SINGLEQUOTE = 7;
  /** RegularExpression Id. */
  int DOUBLEQUOTE = 8;
  /** RegularExpression Id. */
  int LBRACE = 9;
  /** RegularExpression Id. */
  int RBRACE = 10;
  /** RegularExpression Id. */
  int LPAREN = 11;
  /** RegularExpression Id. */
  int RPAREN = 12;
  /** RegularExpression Id. */
  int LBRACKET = 13;
  /** RegularExpression Id. */
  int RBRACKET = 14;
  /** RegularExpression Id. */
  int ASSIGNMENT = 15;
  /** RegularExpression Id. */
  int PLUS = 16;
  /** RegularExpression Id. */
  int MINUS = 17;
  /** RegularExpression Id. */
  int MULT = 18;
  /** RegularExpression Id. */
  int DIV = 19;
  /** RegularExpression Id. */
  int COMMA = 20;
  /** RegularExpression Id. */
  int EQUALS = 21;
  /** RegularExpression Id. */
  int NOTEQUALS = 22;
  /** RegularExpression Id. */
  int STREQUALS = 23;
  /** RegularExpression Id. */
  int STRCONTAINS = 24;
  /** RegularExpression Id. */
  int STRCAT = 25;
  /** RegularExpression Id. */
  int GREATERTHAN = 26;
  /** RegularExpression Id. */
  int LESSTHAN = 27;
  /** RegularExpression Id. */
  int GREATERTHANEQ = 28;
  /** RegularExpression Id. */
  int LESSTHANEQ = 29;
  /** RegularExpression Id. */
  int DO = 30;
  /** RegularExpression Id. */
  int WHEN = 31;
  /** RegularExpression Id. */
  int SET = 32;
  /** RegularExpression Id. */
  int TO = 33;
  /** RegularExpression Id. */
  int RULE = 34;
  /** RegularExpression Id. */
  int SWITCH = 35;
  /** RegularExpression Id. */
  int CASE = 36;
  /** RegularExpression Id. */
  int EVENT = 37;
  /** RegularExpression Id. */
  int TIMER = 38;
  /** RegularExpression Id. */
  int DEFCASE = 39;
  /** RegularExpression Id. */
  int CONDITION = 40;
  /** RegularExpression Id. */
  int CHANGES = 41;
  /** RegularExpression Id. */
  int RETURNS = 42;
  /** RegularExpression Id. */
  int RETURN = 43;
  /** RegularExpression Id. */
  int RESULT = 44;
  /** RegularExpression Id. */
  int TRUE = 45;
  /** RegularExpression Id. */
  int FALSE = 46;
  /** RegularExpression Id. */
  int REFERENCE = 47;
  /** RegularExpression Id. */
  int VARIABLE = 48;
  /** RegularExpression Id. */
  int START = 49;
  /** RegularExpression Id. */
  int DURABLE = 50;
  /** RegularExpression Id. */
  int DYNAMIC = 51;
  /** RegularExpression Id. */
  int AS = 52;
  /** RegularExpression Id. */
  int ON = 53;
  /** RegularExpression Id. */
  int BEFORE = 54;
  /** RegularExpression Id. */
  int AFTER = 55;
  /** RegularExpression Id. */
  int FOREACH = 56;
  /** RegularExpression Id. */
  int IF = 57;
  /** RegularExpression Id. */
  int IN = 58;
  /** RegularExpression Id. */
  int ELSE = 59;
  /** RegularExpression Id. */
  int ELSEIF = 60;
  /** RegularExpression Id. */
  int WHILE = 61;
  /** RegularExpression Id. */
  int BREAK = 62;
  /** RegularExpression Id. */
  int ERROR = 63;
  /** RegularExpression Id. */
  int METHODSEP = 64;
  /** RegularExpression Id. */
  int VALUESEP = 65;
  /** RegularExpression Id. */
  int IS = 66;
  /** RegularExpression Id. */
  int AND = 67;
  /** RegularExpression Id. */
  int OR = 68;
  /** RegularExpression Id. */
  int NOT = 69;
  /** RegularExpression Id. */
  int WAIT = 70;
  /** RegularExpression Id. */
  int UNTIL = 71;
  /** RegularExpression Id. */
  int ARRAY = 72;
  /** RegularExpression Id. */
  int TABLE = 73;
  /** RegularExpression Id. */
  int INTEGER = 74;
  /** RegularExpression Id. */
  int DOUBLE = 75;
  /** RegularExpression Id. */
  int BOOLEAN = 76;
  /** RegularExpression Id. */
  int STRING = 77;
  /** RegularExpression Id. */
  int DATE = 78;
  /** RegularExpression Id. */
  int EXTENSION = 79;
  /** RegularExpression Id. */
  int PRINT = 80;
  /** RegularExpression Id. */
  int NEWLINE = 81;
  /** RegularExpression Id. */
  int name = 82;
  /** RegularExpression Id. */
  int INTEGERLITERAL = 83;
  /** RegularExpression Id. */
  int DATELITERAL = 84;
  /** RegularExpression Id. */
  int FLOATLITERAL = 85;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int IN_SINGLE_LINE_COMMENT = 1;
  /** Lexical state. */
  int IN_MULTI_LINE_COMMENT = 2;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "<WHITESPACE>",
    "\"//\"",
    "\"/*\"",
    "<SINGLE_LINE_COMMENT>",
    "\"*/\"",
    "<token of kind 6>",
    "\"\\\'\"",
    "\"\\\"\"",
    "\"{\"",
    "\"}\"",
    "\"(\"",
    "\")\"",
    "\"[\"",
    "\"]\"",
    "\"=\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\",\"",
    "\"==\"",
    "\"!=\"",
    "<STREQUALS>",
    "<STRCONTAINS>",
    "\"|\"",
    "\">\"",
    "\"<\"",
    "\">=\"",
    "\"<=\"",
    "<DO>",
    "<WHEN>",
    "<SET>",
    "<TO>",
    "<RULE>",
    "<SWITCH>",
    "<CASE>",
    "<EVENT>",
    "<TIMER>",
    "<DEFCASE>",
    "<CONDITION>",
    "<CHANGES>",
    "<RETURNS>",
    "<RETURN>",
    "\"Result\"",
    "<TRUE>",
    "<FALSE>",
    "\"Reference\"",
    "<VARIABLE>",
    "<START>",
    "<DURABLE>",
    "<DYNAMIC>",
    "<AS>",
    "<ON>",
    "<BEFORE>",
    "<AFTER>",
    "<FOREACH>",
    "<IF>",
    "<IN>",
    "<ELSE>",
    "<ELSEIF>",
    "<WHILE>",
    "<BREAK>",
    "<ERROR>",
    "\":\"",
    "\"::\"",
    "<IS>",
    "<AND>",
    "<OR>",
    "<NOT>",
    "<WAIT>",
    "<UNTIL>",
    "\"Array\"",
    "\"Table\"",
    "\"Integer\"",
    "\"Double\"",
    "\"Boolean\"",
    "\"String\"",
    "\"Date\"",
    "\"Extension\"",
    "<PRINT>",
    "\"newLine\"",
    "<name>",
    "<INTEGERLITERAL>",
    "<DATELITERAL>",
    "<FLOATLITERAL>",
    "\";\"",
  };

}
