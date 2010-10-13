/* Generated By:JavaCC: Do not edit this line. CALRuleParserConstants.java */
package com.paxxis.chime.cal.parser;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CALRuleParserConstants {

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
  int TRUE = 44;
  /** RegularExpression Id. */
  int FALSE = 45;
  /** RegularExpression Id. */
  int REFERENCE = 46;
  /** RegularExpression Id. */
  int VARIABLE = 47;
  /** RegularExpression Id. */
  int START = 48;
  /** RegularExpression Id. */
  int SUBMIT = 49;
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
  int IF = 56;
  /** RegularExpression Id. */
  int ELSE = 57;
  /** RegularExpression Id. */
  int ELSEIF = 58;
  /** RegularExpression Id. */
  int WHILE = 59;
  /** RegularExpression Id. */
  int BREAK = 60;
  /** RegularExpression Id. */
  int ERROR = 61;
  /** RegularExpression Id. */
  int METHODSEP = 62;
  /** RegularExpression Id. */
  int VALUESEP = 63;
  /** RegularExpression Id. */
  int IS = 64;
  /** RegularExpression Id. */
  int AND = 65;
  /** RegularExpression Id. */
  int OR = 66;
  /** RegularExpression Id. */
  int NOT = 67;
  /** RegularExpression Id. */
  int WAIT = 68;
  /** RegularExpression Id. */
  int UNTIL = 69;
  /** RegularExpression Id. */
  int ARRAY = 70;
  /** RegularExpression Id. */
  int TABLE = 71;
  /** RegularExpression Id. */
  int INTEGER = 72;
  /** RegularExpression Id. */
  int DOUBLE = 73;
  /** RegularExpression Id. */
  int BOOLEAN = 74;
  /** RegularExpression Id. */
  int STRING = 75;
  /** RegularExpression Id. */
  int DATE = 76;
  /** RegularExpression Id. */
  int STOCKSYMBOL = 77;
  /** RegularExpression Id. */
  int DATAINSTANCE = 78;
  /** RegularExpression Id. */
  int SHAPE = 79;
  /** RegularExpression Id. */
  int EXTENSION = 80;
  /** RegularExpression Id. */
  int QUERY = 81;
  /** RegularExpression Id. */
  int NARROW = 82;
  /** RegularExpression Id. */
  int WHERE = 83;
  /** RegularExpression Id. */
  int RATING = 84;
  /** RegularExpression Id. */
  int FIELD = 85;
  /** RegularExpression Id. */
  int PRINT = 86;
  /** RegularExpression Id. */
  int NEWLINE = 87;
  /** RegularExpression Id. */
  int name = 88;
  /** RegularExpression Id. */
  int INTEGERLITERAL = 89;
  /** RegularExpression Id. */
  int DATELITERAL = 90;
  /** RegularExpression Id. */
  int FLOATLITERAL = 91;

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
    "<TRUE>",
    "<FALSE>",
    "\"Reference\"",
    "<VARIABLE>",
    "<START>",
    "<SUBMIT>",
    "<DURABLE>",
    "<DYNAMIC>",
    "<AS>",
    "<ON>",
    "<BEFORE>",
    "<AFTER>",
    "<IF>",
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
    "\"StockSymbol\"",
    "\"DataInstance\"",
    "\"Shape\"",
    "\"Extension\"",
    "\"Query\"",
    "<NARROW>",
    "<WHERE>",
    "<RATING>",
    "<FIELD>",
    "<PRINT>",
    "\"newLine\"",
    "<name>",
    "<INTEGERLITERAL>",
    "<DATELITERAL>",
    "<FLOATLITERAL>",
    "\";\"",
  };

}
