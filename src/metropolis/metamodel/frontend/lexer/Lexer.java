/* Lexical analyzer for the MetaModel. 
   Lexer.java is derived from the Lexer file.
 */
package metropolis.metamodel.frontend.lexer;
import metropolis.metamodel.frontend.parser.MetaModelParser;
import metropolis.metamodel.frontend.parser.MetaModelParserval;
/**
Lexical analyzer for the MetaModel. 
<p>Lexer.java is derived from the Lexer file by running JLex.
<p>Portions of JLexer are Copyright (C) 1995, 1997 by Paul N. Hilfinger.  All rights reserved.
 Portions of this code were derived from sources developed under the
auspices of the Titanium project, under funding from the DARPA, DoE,
and Army Research Office.
@author Paul N. Hilfinger, Jeff Tsay, Robert Clariso
@version $Id: Lexer,v 1.15 2004/10/19 08:21:21 guyang Exp $
*/


public class Lexer {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final char YY_EOF = '\uFFFF';
	/** Returned by yylex() if we
	 *  are at the end of file.
	 */
	public final int YYEOF = -1;

  /** Return the current line number.
   *  @return The line number.
   */
  public int lineNumber() { return yyline + 1; }
  /** Return the MetaModelParserval.
   *  @return The MetaModelParserval
   */ 	
  public MetaModelParserval getMetaModelParserval() { return returnVal; }
  /** Integer 0 Metamodel Parser value. **/
  protected MetaModelParserval returnVal = new MetaModelParserval(0);
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yyline;
	private int yy_lexical_state;

	/** Create a Lexer that reads from
	 *  a Reader.
	 *  @param reader The Reader.
	 */
	public Lexer (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	/** Create a Lexer that reads from
	 *  an InputStream.
	 *  @param instream The InputStream.
	 */
	public Lexer (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Lexer () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yyline = 0;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int YYINITIAL = 0;
	private final int yy_state_dtrans[] = {
		0
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private char yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_start () {
		if ((byte) '\n' == yy_buffer[yy_buffer_start]
			|| (byte) '\r' == yy_buffer[yy_buffer_start]) {
			++yyline;
		}
		++yy_buffer_start;
	}
	private void yy_pushback () {
		--yy_buffer_end;
	}
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ((byte) '\n' == yy_buffer[i] || (byte) '\r' == yy_buffer[i]) {
				++yyline;
			}
		}
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
private int [][] unpackFromString(int size1, int size2, String st)
    {
      int colonIndex = -1;
      String lengthString;
      int sequenceLength = 0;
      int sequenceInteger = 0;
      int commaIndex;
      String workString;
      int res[][] = new int[size1][size2];
      for (int i= 0; i < size1; i++)
        for (int j= 0; j < size2; j++)
          {
            if (sequenceLength == 0) 
              {        
                commaIndex = st.indexOf(',');
                if (commaIndex == -1)
                  workString = st;
                else
                  workString = st.substring(0, commaIndex);
                st = st.substring(commaIndex+1);
                colonIndex = workString.indexOf(':');
                if (colonIndex == -1)
                  {
                    res[i][j] = Integer.parseInt(workString);
                  }
                else 
                  {
                    lengthString = workString.substring(colonIndex+1);  
                    sequenceLength = Integer.parseInt(lengthString);
                    workString = workString.substring(0,colonIndex);
                    sequenceInteger = Integer.parseInt(workString);
                    res[i][j] = sequenceInteger;
                    sequenceLength--;
                  }
              }
            else 
              {
                res[i][j] = sequenceInteger;
                sequenceLength--;
              }
          }
      return res;
    }
	private int yy_acpt[] = {
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NOT_ACCEPT,
		YY_NOT_ACCEPT,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR,
		YY_NO_ANCHOR
	};
	private int yy_cmap[] = {
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 1, 2, 0, 3, 4, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		1, 5, 6, 0, 7, 8, 9, 10,
		11, 11, 12, 13, 11, 14, 15, 16,
		17, 18, 18, 18, 19, 19, 19, 19,
		20, 20, 21, 11, 22, 23, 24, 21,
		25, 26, 27, 27, 28, 29, 30, 31,
		32, 33, 32, 32, 34, 32, 32, 32,
		32, 32, 32, 35, 36, 37, 32, 32,
		38, 32, 32, 39, 40, 41, 42, 32,
		0, 43, 44, 45, 46, 47, 48, 49,
		50, 51, 32, 52, 53, 54, 55, 56,
		57, 58, 59, 60, 61, 62, 63, 64,
		65, 66, 67, 68, 69, 70, 21, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0,
		0 
	};
	private int yy_rmap[] = {
		0, 1, 2, 3, 1, 4, 5, 6,
		7, 7, 8, 7, 7, 9, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1,
		1, 1, 1, 10, 1, 1, 1, 1,
		11, 1, 1, 1, 1, 1, 12, 1,
		1, 13, 7, 14, 1, 1, 1, 1,
		1, 1, 15, 1, 1, 1, 16, 7,
		7, 7, 7, 17, 18, 7, 7, 7,
		7, 7, 1, 1, 1, 1, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 19, 7, 7, 7, 7,
		7, 7, 7, 7, 20, 7, 7, 7,
		7, 7, 7, 7, 7, 21, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 7, 7, 7, 7,
		7, 7, 7, 7, 22, 22, 23, 24,
		25, 26, 1, 22, 1, 27, 28, 29,
		30, 31, 32, 33, 34, 35, 36, 37,
		38, 39, 27, 40, 41, 42, 43, 44,
		45, 46, 47, 48, 1, 49, 50, 51,
		52, 53, 54, 55, 56, 57, 58, 59,
		60, 61, 62, 63, 64, 65, 66, 67,
		68, 69, 70, 71, 72, 73, 74, 75,
		76, 77, 78, 79, 80, 81, 82, 83,
		84, 85, 86, 87, 88, 89, 90, 91,
		92, 93, 94, 95, 96, 97, 98, 99,
		100, 101, 102, 103, 104, 105, 106, 107,
		108, 109, 110, 111, 112, 113, 114, 115,
		116, 117, 118, 119, 120, 121, 122, 123,
		124, 125, 126, 127, 128, 129, 130, 131,
		132, 133, 134, 135, 136, 137, 138, 139,
		140, 141, 142, 143, 144, 145, 146, 147,
		148, 149, 150, 151, 152, 153, 154, 155,
		156, 157, 158, 159, 160, 161, 162, 163,
		164, 165, 166, 167, 168, 169, 170, 171,
		172, 173, 174, 175, 176, 177, 178, 179,
		180, 34, 181, 71, 182, 183, 184, 185,
		186, 187, 188, 189, 190, 191, 192, 193,
		194, 195, 196, 197, 198, 199, 200, 201,
		202, 203, 204, 205, 206, 207, 208, 209,
		210, 211, 212, 213, 214, 215, 216, 217,
		218, 219, 220, 221, 222, 223, 224, 225,
		226, 227, 228, 229, 230, 231, 232, 233,
		234, 235, 236, 237, 238, 239, 240, 241,
		242, 243, 244, 245, 246, 247, 248, 249,
		250, 251, 252, 253, 254, 255, 256, 257,
		258, 259, 260, 261, 262, 263, 264, 265,
		266, 267, 268, 269, 270, 271, 272, 273,
		274, 275, 276, 277, 278, 279, 280, 281,
		282, 283, 284, 285, 286, 287, 288, 289,
		290, 291, 292, 293, 294, 295, 296, 297,
		298, 299, 300, 301, 302, 303, 304, 305,
		306, 307, 308, 309, 310, 311, 312, 313,
		314, 315, 316, 317, 318, 319, 320, 321,
		322, 323, 324, 325, 326, 327, 328, 329,
		330, 331, 332, 333, 334, 335, 336, 337,
		338, 339, 340, 341, 342, 343, 344, 345,
		346, 347, 348, 349, 350, 351, 352, 353,
		354, 355, 356, 357, 358, 359, 360, 361,
		362, 363, 364, 365, 366, 367, 368, 369,
		370, 371, 372, 373, 374, 375, 376, 377,
		378, 379, 380, 381, 382, 383, 384, 385,
		386, 387, 388, 389, 390, 391, 392, 393,
		394, 395, 396, 397, 398, 399, 400, 401,
		402, 403, 404, 405, 406, 407, 408, 409,
		410, 411, 412, 413, 414, 415, 416, 417,
		418, 419, 420, 421, 422, 423, 424, 425,
		426, 427, 428, 429, 430, 431, 432, 433,
		434, 435, 436, 437, 438, 439, 440, 441,
		442, 443, 444, 445, 446, 447, 448, 449,
		450, 451, 452, 453, 454, 455, 456, 457,
		458, 459, 460, 461, 462, 463, 464, 465,
		466, 467, 468, 469, 470, 471, 472, 473,
		474, 475, 476, 477, 478, 479, 480, 481,
		482, 483, 484, 485, 486, 487, 488, 489,
		490, 491, 492, 493, 494, 495, 496, 497,
		498, 499, 500, 501, 502, 503, 504, 505,
		506, 507, 508, 509, 510, 511, 512, 513,
		514, 515, 516, 517, 518, 519, 520, 521,
		522, 523, 524, 525, 526, 527, 528, 529,
		530, 531, 532, 533, 534, 535, 536, 537,
		538, 539, 540, 541 
	};
	private int yy_nxt[][] = unpackFromString(542,71,
"1,2:4,3,165,176,166,177,182,4,183,187,190,167,193,5,6:3,196,199,202,205,7,8:4,9,10,8:2,440,8:2,11,12,13,1,4,208,328,334,441,169,335,336,442,8,179,8,337,524,338,525,184,650,586,526,339,587,443,527,8:3,178,211,4,-1:72,2:4,-1:89,14,-1:62,329,-1,168:3,195,-1:7,172,198,29,-1:3,30,-1:3,201,-1:7,172,198,29,-1:4,30,-1:11,201,-1:20,329,-1,6:4,-1:7,172,198,29,-1:3,31,-1:11,172,198,29,-1:4,31,-1:87,39,-1:17,8:4,-1:5,8:13,-1:4,8:25,-1:20,8:4,-1:5,8:12,188,-1:4,8:25,-1:4,13:2,-1:13,207,-1:24,170,-1:46,27:4,-1:7,172,198,29,-1:15,172,198,29,-1:45,52,-1:70,53,54,-1:63,8:4,-1:5,8:13,-1:4,8:19,460,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:20,366,8:4,-1:20,50:4,-1:5,50:5,-1:3,68,-1:8,50:6,-1:4,68,-1:40,69,-1:64,8:4,-1:5,8:13,-1:4,375,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,604,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:10,280,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:16,643,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:2,644,8:22,-1:3,164:2,-1,164:3,15,164:33,175,164:30,-1:8,181,-1:14,17,-1:64,27:4,-1:65,329,-1,168:3,195,-1:7,172,198,29,-1:3,30,-1:11,172,198,29,-1:4,30,-1:34,8:4,-1:5,8:13,-1:4,8:4,589,8:8,41,8:11,-1:13,48,-1:77,8:4,-1:5,8:13,-1:4,8:17,65,8:7,-1:3,164:2,-1,164:3,171,164:33,175,164:30,-1:70,16,-1:9,18,-1:13,19,-1:54,44,-1:17,45,-1:62,8:4,-1:5,8:13,-1:4,8:5,42,8:5,528,203,8:4,691,8:7,-1:20,180:4,-1:7,172,198,29,-1:15,172,198,29,-1:22,181:8,210,181:62,186:2,-1,186:7,-1,186:29,189,186:30,-1:23,20,-1:64,8:4,-1:5,8:13,-1:4,593,8,43,8,531,8:8,350,8:2,594,8:2,532,8:5,-1:20,185:4,-1:7,172,-1,29,-1:15,172,-1,29,-1:35,21,-1:9,22,-1:64,8:4,-1:5,8:7,55,8:5,-1:4,8:25,-1:9,186,-1:3,173,-1:6,213:2,215,-1:20,186,-1:3,186,-1:3,186,-1:6,186,-1:3,186,-1,186,217,-1:22,23,-1:7,24,25,26,-1:63,8:4,-1:5,8:13,-1:4,8:10,56,8:14,-1:3,192:2,49,192,49,192:66,-1:12,331,-1:3,192,-1:6,28,-1:64,8:4,-1:5,8:13,-1:4,8:6,57,8:18,-1:18,329,-1,195:4,-1:7,172,198,29,-1:15,172,198,29,-1:39,8:4,-1:5,8:13,-1:4,8:3,58,8:21,-1:16,223:2,-1:2,185:4,-1:64,204,-1:7,32,33,-1:64,8:4,-1:5,8:13,-1:4,8:16,59,8:8,-1:20,50:4,-1:5,50:5,-1:12,50:6,-1:45,34,35,-1:63,8:4,-1:5,8:13,-1:4,8:17,652,60,8:6,-1:27,51,-1:60,36,-1:8,37,38,-1:63,8:4,-1:5,8:13,-1:4,8:13,61,8:11,-1:15,225,-1:3,227,-1:77,40,-1:64,8:4,-1:5,8:13,-1:4,8:2,62,8:9,235,8:12,-1:3,181:8,66,181:62,-1:23,46,-1:45,47,-1:18,8:4,-1:5,8:13,-1:4,8:10,63,8:14,-1:13,48,-1:6,215:3,-1:68,8:4,-1:5,8:13,-1:4,8:18,543,8:2,64,8:3,-1:13,48,-1:6,186:3,-1:68,8:4,-1:5,8:13,-1:4,633,8:18,240,8:3,65,8,-1:20,229:4,-1:5,229:5,-1:12,229:6,-1:13,217,-1:25,8:4,-1:5,8:10,70,8:2,-1:4,8:25,-1:3,219:12,221,219:58,-1:17,8:4,-1:5,8:13,-1:4,8:4,71,8:20,-1:3,219:12,221,219:3,67,219:54,-1:17,8:4,-1:5,8:13,-1:4,8:4,72,8:20,-1:20,185:4,-1:67,8:4,-1:5,8:13,-1:4,8:16,73,8:8,-1:3,225:12,231,225:58,-1:17,8:4,-1:5,8:13,-1:4,8:2,74,8:22,-1:3,227:2,13,227,13,227:66,-1:17,8:4,-1:5,8:13,-1:4,8:4,75,8:20,-1:20,332:4,-1:5,332:5,-1:12,332:6,-1:39,8:4,-1:5,8:13,-1:4,8:10,76,8:14,-1:3,225:12,231,225:3,13,225:54,-1:17,8:4,-1:5,8:13,-1:4,8:10,77,8:14,-1:20,186:4,-1:5,186:5,-1:12,186:6,-1:39,8:4,-1:5,8:13,-1:4,8:13,65,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:6,78,8:18,-1:20,8:4,-1:5,8:13,-1:4,8:3,676,79,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:10,80,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,81,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:17,82,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:4,83,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:3,84,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:18,85,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:9,86,8:15,-1:20,8:4,-1:5,8:13,-1:4,8:9,87,8:15,-1:20,8:4,-1:5,8:13,-1:4,8:7,65,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:17,88,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:18,330,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,89,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:4,90,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:10,91,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,92,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:10,93,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:22,94,8:2,-1:20,8:4,-1:5,8:13,-1:4,8:16,95,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:10,96,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,97,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:10,98,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:16,99,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:7,100,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:21,174,8:3,-1:20,8:4,-1:5,8:13,-1:4,8:4,101,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,102,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:17,103,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:10,104,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,105,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:11,106,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:4,65,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:3,107,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:2,108,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,109,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,110,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:10,111,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:2,112,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:7,113,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:4,114,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,115,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:18,116,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,117,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:17,118,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:23,65,8,-1:20,8:4,-1:5,8:13,-1:4,8:4,119,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,120,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,121,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:18,122,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:4,123,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,124,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:17,125,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:18,126,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,127,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:22,128,8:2,-1:20,8:4,-1:5,8:13,-1:4,8:18,129,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:4,130,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,131,8:20,-1:20,8:4,-1:5,8:13,-1:4,132,8:24,-1:20,8:4,-1:5,8:13,-1:4,133,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:23,134,8,-1:20,8:4,-1:5,8:13,-1:4,8:23,135,8,-1:20,8:4,-1:5,8:13,-1:4,8:4,136,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:14,65,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:4,137,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,138,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:3,139,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:4,140,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:16,141,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:3,142,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:16,143,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:18,144,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,145,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:11,146,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:17,147,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:17,148,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:5,149,8:19,-1:20,8:4,-1:5,8:13,-1:4,8:14,150,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:4,151,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,152,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:11,153,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:18,154,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,155,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:3,65,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:18,156,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:11,157,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:18,158,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:11,159,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:18,160,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,161,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:3,162,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:18,163,8:6,-1:20,8:4,-1:5,8:13,-1:4,8,626,8,675,8:6,191,8:10,444,8:3,-1:20,8:4,-1:5,8:13,-1:4,401,8:15,567,8:8,-1:20,233:4,-1:5,233:5,-1:12,233:6,-1:39,8:4,-1:5,8:13,-1:4,8:18,65,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:4,194,8:5,445,8:2,588,8:2,446,8:6,341,8,-1:20,8:4,-1:5,8:13,-1:4,8:10,344,8,197,8:7,345,8,346,8:2,-1:20,8:4,-1:5,8:13,-1:4,449,8:7,450,8,451,8:2,200,8:11,-1:20,8:4,-1:5,8:13,-1:4,452,8:4,206,8:7,209,8:4,212,8:6,-1:20,8:4,-1:5,8:13,-1:4,530,8:3,214,8:8,348,8:5,349,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:4,629,8:2,351,8:8,216,8:8,-1:20,8:4,-1:5,8:9,218,8:3,-1:4,8:25,-1:20,8:4,-1:5,8:13,-1:4,8:18,220,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:17,222,356,8:6,-1:20,8:4,-1:5,8:13,-1:4,224,8:24,-1:20,8:4,-1:5,8:13,-1:4,630,8:12,226,8:3,228,8:7,-1:20,8:4,-1:5,8:13,-1:4,230,8:3,359,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,232,8:5,461,8:9,539,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,234,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:12,236,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:10,237,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:16,238,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:8,239,8:7,371,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:8,241,8,602,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:8,242,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:2,243,8:22,-1:20,8:4,-1:5,8:13,-1:4,244,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:2,245,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:17,246,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:12,473,8:4,247,550,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:12,248,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:17,249,8:7,-1:20,8:4,-1:5,8:13,-1:4,250,8:24,-1:20,8:4,-1:5,8:13,-1:4,251,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,252,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,253,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,254,8:20,-1:20,8:4,-1:5,8:13,-1:4,255,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:16,256,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:19,257,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:4,258,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,259,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:13,260,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:10,261,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:10,262,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,263,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:10,264,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:16,265,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:19,266,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:20,267,8:4,-1:20,8:4,-1:5,8:13,-1:4,8:13,268,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:8,269,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:12,270,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:16,271,8:8,-1:20,8:4,-1:5,8:13,-1:4,272,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,641,8:3,273,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:2,274,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:18,275,8:6,-1:20,8:4,-1:5,8:13,-1:4,276,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:2,277,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:10,278,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:3,279,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:14,281,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:18,282,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,283,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:17,284,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:6,285,8:18,-1:20,8:4,-1:5,8:13,-1:4,8:18,286,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:17,287,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:16,288,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:2,289,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:13,290,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:12,291,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:19,292,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:14,293,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:18,294,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,295,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,296,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,297,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:14,298,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:5,299,8:19,-1:20,8:4,-1:5,8:13,-1:4,8:18,300,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:10,267,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,301,8:6,-1:20,8:4,-1:5,8:13,-1:4,302,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:2,303,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,304,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,305,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,306,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,333,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,307,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:16,308,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:19,309,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:17,310,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:18,311,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:13,312,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:13,313,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:11,314,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:11,315,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:19,316,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:12,317,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,318,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:4,319,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,320,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:17,321,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:2,322,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:19,323,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:17,324,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:16,325,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:4,326,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:16,327,8:8,-1:20,8:4,-1:5,340,8:12,-1:4,8:25,-1:20,8:4,-1:5,8:13,-1:4,342,8:6,343,8:2,447,8:2,448,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:4,590,8:8,347,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:13,352,8:11,-1:20,8:4,-1:5,8:13,-1:4,353,8:24,-1:20,8:4,-1:5,8:13,-1:4,597,8:12,354,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:4,355,8:20,-1:20,8:4,-1:5,8:13,-1:4,357,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:12,358,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:10,360,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:12,361,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,362,8:11,-1:20,8:4,-1:5,8:13,-1:4,8,363,8:23,-1:20,8:4,-1:5,8:13,-1:4,8:18,364,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:7,365,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:13,367,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:11,368,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:14,369,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:12,370,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:8,372,8:16,-1:20,8:4,-1:5,8:13,-1:4,8,373,8:23,-1:20,8:4,-1:5,8:13,-1:4,8:17,374,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:10,636,8:2,376,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:8,377,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:8,378,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:8,379,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:10,380,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:8,381,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:19,382,383,8:4,-1:20,8:4,-1:5,8:13,-1:4,8:18,384,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,385,8:6,-1:20,8:4,-1:5,8:13,-1:4,386,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,387,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,388,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:19,389,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:12,390,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:7,561,8:15,391,8,-1:20,8:4,-1:5,8:13,-1:4,392,8:24,-1:20,8:4,-1:5,8:13,-1:4,393,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:8,394,8:16,-1:20,8:4,-1:5,8:13,-1:4,395,8:24,-1:20,8:4,-1:5,8:13,-1:4,396,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,397,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:13,398,8:11,-1:20,8:4,-1:5,8:13,-1:4,399,8:24,-1:20,8:4,-1:5,8:13,-1:4,8,400,8:23,-1:20,8:4,-1:5,8:13,-1:4,8:12,402,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,403,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:10,404,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:10,405,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:8,406,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:8,407,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:13,408,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:18,409,8:6,-1:20,8:4,-1:5,8:13,-1:4,410,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:8,411,8:16,-1:20,8:4,-1:5,8:13,-1:4,412,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,413,8:20,-1:20,8:4,-1:5,8:13,-1:4,414,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:18,415,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,416,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:10,417,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:4,418,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:8,419,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:13,420,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:12,421,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:4,422,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,423,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:4,424,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:13,425,8:11,-1:20,8:4,-1:5,8:13,-1:4,426,8:24,-1:20,8:4,-1:5,8:13,-1:4,427,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:8,428,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:4,429,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,430,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:24,431,-1:20,8:4,-1:5,8:13,-1:4,8:4,432,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:8,433,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:4,434,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:3,520,8:8,435,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:4,436,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:14,523,8:2,437,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:12,438,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,439,8:11,-1:20,8:4,-1:5,8:13,-1:4,591,8:3,529,8:3,592,8:10,453,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:18,454,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,651,8,628,8:2,455,456,8:9,534,457,8,535,8,458,8,-1:20,8:4,-1:5,8:13,-1:4,8:7,459,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:14,462,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:3,463,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:18,464,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:16,465,8:8,-1:20,8:4,-1:5,8:13,-1:4,8,466,8:23,-1:20,8:4,-1:5,8:13,-1:4,8:3,684,8,467,8:12,468,8:6,-1:20,8:4,-1:5,8:13,-1:4,469,8:15,600,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:8,470,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:3,471,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:10,472,8:14,-1:20,8:4,-1:5,8:13,-1:4,474,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,475,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,654,8:5,655,8:3,634,8,635,8:2,551,476,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:3,552,8:12,477,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:3,553,8:12,478,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:10,479,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:9,480,8:15,-1:20,8:4,-1:5,8:13,-1:4,8:13,554,8:6,481,8:4,-1:20,8:4,-1:5,8:13,-1:4,8:2,482,8:15,606,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:14,483,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:16,484,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:9,485,8:15,-1:20,8:4,-1:5,8:13,-1:4,8:8,486,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:2,487,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,488,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,489,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:16,490,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:18,491,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,492,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:2,493,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:10,494,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:18,495,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:16,496,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:16,497,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:5,498,8:19,-1:20,8:4,-1:5,8:13,-1:4,8:4,499,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,500,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:19,501,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:8,502,8:16,-1:20,8:4,-1:5,8:13,-1:4,503,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:2,685,8:11,504,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:18,505,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,506,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,507,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:2,508,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:10,509,8:14,-1:20,8:4,-1:5,8:13,-1:4,8:12,510,578,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:12,511,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:3,512,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:12,513,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,514,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:8,515,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:12,516,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,517,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,518,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,519,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,521,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:8,522,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:4,533,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:14,536,8:2,595,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:13,537,8:5,662,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:5,538,8:19,-1:20,8:4,-1:5,8:13,-1:4,8:18,540,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:22,541,8:2,-1:20,8:4,-1:5,8:13,-1:4,8:12,542,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:2,544,8:13,631,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:8,545,8:4,546,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:4,547,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:18,548,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,549,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:12,555,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:17,556,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:8,557,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:14,558,8:10,-1:20,8:4,-1:5,8:13,-1:4,559,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:13,560,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:16,562,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:11,563,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:4,564,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:3,565,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:17,566,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:7,568,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:16,569,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:13,570,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:11,571,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:12,572,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:3,573,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:14,574,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:18,575,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:4,576,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:13,577,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:12,579,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,580,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:8,581,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:12,582,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,583,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:13,584,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:5,585,8:19,-1:20,8:4,-1:5,8:13,-1:4,8:17,596,8:7,-1:20,8:4,-1:5,8:13,-1:4,598,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:18,599,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:11,601,8:13,-1:20,8:4,-1:5,8:13,-1:4,8,603,8:23,-1:20,8:4,-1:5,8:13,-1:4,605,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,607,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,608,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:18,609,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:13,610,8:2,611,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:4,612,8:20,-1:20,8:4,-1:5,8:13,-1:4,613,8:24,-1:20,8:4,-1:5,8:13,-1:4,8:4,614,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:11,615,678,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:17,616,8:7,-1:20,8:4,-1:5,8:13,-1:4,8:11,617,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:14,618,8:10,-1:20,8:4,-1:5,8:13,-1:4,8:13,619,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:13,620,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:11,621,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:13,622,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:8,623,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:8,624,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:4,625,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:19,627,8:5,-1:20,8:4,-1:5,8:13,-1:4,8:7,632,8:17,-1:20,8:4,-1:5,8:13,-1:4,8:18,637,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:3,638,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:13,639,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:12,640,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:11,642,8:13,-1:20,8:4,-1:5,8:13,-1:4,8:16,645,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:2,646,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:18,647,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:18,648,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:16,649,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:12,653,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,656,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:4,657,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:18,658,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,659,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:2,660,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:12,661,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:2,663,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:18,664,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:2,665,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,666,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,667,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:13,668,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:3,669,8:21,-1:20,8:4,-1:5,8:13,-1:4,8:4,670,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:4,671,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,672,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,673,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:8,674,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:16,677,8:8,-1:20,8:4,-1:5,8:13,-1:4,8:12,679,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:18,680,8:6,-1:20,8:4,-1:5,8:13,-1:4,8:8,681,8:16,-1:20,8:4,-1:5,8:13,-1:4,8:13,682,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:2,683,8:22,-1:20,8:4,-1:5,8:13,-1:4,8:4,686,8:20,-1:20,8:4,-1:5,8:13,-1:4,8:12,687,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:12,688,8:12,-1:20,8:4,-1:5,8:13,-1:4,8:13,689,8:11,-1:20,8:4,-1:5,8:13,-1:4,8:2,690,8:22,-1:3");
	/** Perform lexical analysis.
	 *  @return The token.
	 *  @exception java.io.IOException If there
	 *  are problems reading data.
	 */
	public int yylex ()
		throws java.io.IOException {
		char yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			if (YY_EOF != yy_lookahead) {
				yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_EOF == yy_lookahead && true == yy_initial) {
					return YYEOF;
				}
				else if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_to_mark();
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_pushback();
					}
					if (0 != (YY_START & yy_anchor)) {
						yy_move_start();
					}
					switch (yy_last_accept_state) {
					case 1:
						{  throw new RuntimeException("Lexical error : Line " + 
                         (1 + yyline) + " Unmatched input: " + yytext()); }
					case -2:
						break;
					case 2:
						{ }
					case -3:
						break;
					case 3:
						{ return yytext().charAt(0); }
					case -4:
						break;
					case 4:
						{ return yytext().charAt(0); }
					case -5:
						break;
					case 5:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.INT_LITERAL; }
					case -6:
						break;
					case 6:
						{ returnVal = new MetaModelParserval(yytext());
	          return MetaModelParser.INT_LITERAL; }
					case -7:
						break;
					case 7:
						{ return MetaModelParser.AT; }
					case -8:
						break;
					case 8:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -9:
						break;
					case 9:
						{ return MetaModelParser.F; }
					case -10:
						break;
					case 10:
						{ return MetaModelParser.G; }
					case -11:
						break;
					case 11:
						{ return MetaModelParser.U; }
					case -12:
						break;
					case 12:
						{ return MetaModelParser.X; }
					case -13:
						break;
					case 13:
						{ String s = yytext();
                  if (s.charAt(s.length() - 1) == ']') 
                      return MetaModelParser.EMPTY_DIM;
                  else 
                      return yytext().charAt(0); }
					case -14:
						break;
					case 14:
						{ return MetaModelParser.NE; }
					case -15:
						break;
					case 15:
						{ String s = yytext();
                  s = s.substring(1,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.STRING_LITERAL; }
					case -16:
						break;
					case 16:
						{ return MetaModelParser.END_ANNOTATION; }
					case -17:
						break;
					case 17:
						{ return MetaModelParser.REM_ASG; }
					case -18:
						break;
					case 18:
						{ return MetaModelParser.CAND; }
					case -19:
						break;
					case 19:
						{ return MetaModelParser.AND_ASG; }
					case -20:
						break;
					case 20:
						{ return MetaModelParser.MULT_ASG; }
					case -21:
						break;
					case 21:
						{ return MetaModelParser.PLUSPLUS; }
					case -22:
						break;
					case 22:
						{ return MetaModelParser.PLUS_ASG; }
					case -23:
						break;
					case 23:
						{ return MetaModelParser.MINUSMINUS; }
					case -24:
						break;
					case 24:
						{ return MetaModelParser.BEGIN_TEMPLATE; }
					case -25:
						break;
					case 25:
						{ return MetaModelParser.MINUS_ASG; }
					case -26:
						break;
					case 26:
						{ return MetaModelParser.CIF; }
					case -27:
						break;
					case 27:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.DOUBLE_LITERAL; }
					case -28:
						break;
					case 28:
						{ return MetaModelParser.DIV_ASG; }
					case -29:
						break;
					case 29:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.FLOAT_LITERAL; }
					case -30:
						break;
					case 30:
						{ String s = yytext();
                  s = s.substring(0,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.LONG_LITERAL; }
					case -31:
						break;
					case 31:
						{ String s = yytext();
                  s = s.substring(0,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.LONG_LITERAL; }
					case -32:
						break;
					case 32:
						{ return MetaModelParser.LSHIFTL; }
					case -33:
						break;
					case 33:
						{ return MetaModelParser.LE; }
					case -34:
						break;
					case 34:
						{ return MetaModelParser.EQ; }
					case -35:
						break;
					case 35:
						{ return MetaModelParser.IMPLY; }
					case -36:
						break;
					case 36:
						{ return MetaModelParser.END_TEMPLATE; }
					case -37:
						break;
					case 37:
						{ return MetaModelParser.GE; }
					case -38:
						break;
					case 38:
						{ return MetaModelParser.ASHIFTR; }
					case -39:
						break;
					case 39:
						{ return MetaModelParser.END_LABEL; }
					case -40:
						break;
					case 40:
						{ return MetaModelParser.XOR_ASG; }
					case -41:
						break;
					case 41:
						{ return MetaModelParser.DO; }
					case -42:
						break;
					case 42:
						{ return MetaModelParser.IF; }
					case -43:
						break;
					case 43:
						{ return MetaModelParser.PC; }
					case -44:
						break;
					case 44:
						{ return MetaModelParser.BEGIN_ANNOTATION; }
					case -45:
						break;
					case 45:
						{ return MetaModelParser.BEGIN_LABEL; }
					case -46:
						break;
					case 46:
						{ return MetaModelParser.OR_ASG; }
					case -47:
						break;
					case 47:
						{ return MetaModelParser.COR; }
					case -48:
						break;
					case 48:
						{ String s = yytext();
                  s = s.substring(1,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.CHARACTER_LITERAL; }
					case -49:
						break;
					case 49:
						{ }
					case -50:
						break;
					case 50:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.INT_LITERAL; }
					case -51:
						break;
					case 51:
						{ return MetaModelParser.CIFF; }
					case -52:
						break;
					case 52:
						{ return MetaModelParser.LSHIFTL_ASG; }
					case -53:
						break;
					case 53:
						{ return MetaModelParser.ASHIFTR_ASG; }
					case -54:
						break;
					case 54:
						{ return MetaModelParser.LSHIFTR; }
					case -55:
						break;
					case 55:
						{ return MetaModelParser.EXECINDEX; }
					case -56:
						break;
					case 56:
						{ return MetaModelParser.ALL; }
					case -57:
						break;
					case 57:
						{ return MetaModelParser.BEG; }
					case -58:
						break;
					case 58:
						{ return MetaModelParser.END; }
					case -59:
						break;
					case 59:
						{ return MetaModelParser.FOR; }
					case -60:
						break;
					case 60:
						{ return MetaModelParser.INT; }
					case -61:
						break;
					case 61:
						{ return MetaModelParser.LFO; }
					case -62:
						break;
					case 62:
						{ return MetaModelParser.LOC; }
					case -63:
						break;
					case 63:
						{ return MetaModelParser.LTL; }
					case -64:
						break;
					case 64:
						{ return MetaModelParser.NEW; }
					case -65:
						break;
					case 65:
						{ throw new RuntimeException("reserved keyword " + yytext() +
                        " ignored"); }
					case -66:
						break;
					case 66:
						{ String s = yytext();
                  s = s.substring(2,s.length()-2);
	          returnVal = new MetaModelParserval(s);
	          return MetaModelParser.BLACKBOX_CONTENT; }
					case -67:
						break;
					case 67:
						{ }
					case -68:
						break;
					case 68:
						{ String s = yytext();
                  s = s.substring(0,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.LONG_LITERAL; }
					case -69:
						break;
					case 69:
						{ return MetaModelParser.LSHIFTR_ASG; }
					case -70:
						break;
					case 70:
						{ return MetaModelParser.LAST; }
					case -71:
						break;
					case 71:
						{ return MetaModelParser.BYTE; }
					case -72:
						break;
					case 72:
						{ return MetaModelParser.CASE; }
					case -73:
						break;
					case 73:
						{ return MetaModelParser.CHAR; }
					case -74:
						break;
					case 74:
						{ return MetaModelParser.ELOC; }
					case -75:
						break;
					case 75:
						{ return MetaModelParser.ELSE; }
					case -76:
						break;
					case 76:
						{ return MetaModelParser.EVAL; }
					case -77:
						break;
					case 77:
						{ return MetaModelParser.EXCL; }
					case -78:
						break;
					case 78:
						{ return MetaModelParser.LONG; }
					case -79:
						break;
					case 79:
						{ return MetaModelParser.NONE; }
					case -80:
						break;
					case 80:
						{ return MetaModelParser._NULL; }
					case -81:
						break;
					case 81:
						{ return MetaModelParser.PORT; }
					case -82:
						break;
					case 82:
						{ return MetaModelParser.THIS; }
					case -83:
						break;
					case 83:
						{ return MetaModelParser.TRUE; }
					case -84:
						break;
					case 84:
						{ return MetaModelParser.VOID; }
					case -85:
						break;
					case 85:
						{ return MetaModelParser.AWAIT; }
					case -86:
						break;
					case 86:
						{ return MetaModelParser.BLOCK; }
					case -87:
						break;
					case 87:
						{ return MetaModelParser.BREAK; }
					case -88:
						break;
					case 88:
						{ return MetaModelParser.CLASS; }
					case -89:
						break;
					case 89:
						{ return MetaModelParser.EVENT; }
					case -90:
						break;
					case 90:
						{ return MetaModelParser.FALSE; }
					case -91:
						break;
					case 91:
						{ return MetaModelParser.FINAL; }
					case -92:
						break;
					case 92:
						{ return MetaModelParser.FLOAT; }
					case -93:
						break;
					case 93:
						{ return MetaModelParser.LABEL; }
					case -94:
						break;
					case 94:
						{ return MetaModelParser.MUTEX; }
					case -95:
						break;
					case 95:
						{ return MetaModelParser.OTHER; }
					case -96:
						break;
					case 96:
						{ return MetaModelParser.PCVAL; }
					case -97:
						break;
					case 97:
						{ return MetaModelParser.SHORT; }
					case -98:
						break;
					case 98:
						{ return MetaModelParser.SIMUL; }
					case -99:
						break;
					case 99:
						{ return MetaModelParser.SUPER; }
					case -100:
						break;
					case 100:
						{ return MetaModelParser.SYNCH; }
					case -101:
						break;
					case 101:
						{ return MetaModelParser.WHILE; }
					case -102:
						break;
					case 102:
						{ return MetaModelParser.DOUBLE; }
					case -103:
						break;
					case 103:
						{ return MetaModelParser.EXISTS; }
					case -104:
						break;
					case 104:
						{ return MetaModelParser.FORALL; }
					case -105:
						break;
					case 105:
						{ return MetaModelParser.IMPORT; }
					case -106:
						break;
					case 106:
						{ return MetaModelParser.MEDIUM; }
					case -107:
						break;
					case 107:
						{ return MetaModelParser.PERIOD; }
					case -108:
						break;
					case 108:
						{ return MetaModelParser.PUBLIC; }
					case -109:
						break;
					case 109:
						{ return MetaModelParser.REFINE; }
					case -110:
						break;
					case 110:
						{ return MetaModelParser.RETURN; }
					case -111:
						break;
					case 111:
						{ return MetaModelParser.RETVAL; }
					case -112:
						break;
					case 112:
						{ return MetaModelParser.STATIC; }
					case -113:
						break;
					case 113:
						{ return MetaModelParser.SWITCH; }
					case -114:
						break;
					case 114:
						{ return MetaModelParser.UPDATE; }
					case -115:
						break;
					case 115:
						{ return MetaModelParser.BOOLEAN; }
					case -116:
						break;
					case 116:
						{ return MetaModelParser.CONNECT; }
					case -117:
						break;
					case 117:
						{ return MetaModelParser.DEFAULT; }
					case -118:
						break;
					case 118:
						{ return MetaModelParser.EXTENDS; }
					case -119:
						break;
					case 119:
						{ return MetaModelParser.GETTYPE; }
					case -120:
						break;
					case 120:
						{ return MetaModelParser.MAXRATE; }
					case -121:
						break;
					case 121:
						{ return MetaModelParser.MINRATE; }
					case -122:
						break;
					case 122:
						{ return MetaModelParser.NETLIST; }
					case -123:
						break;
					case 123:
						{ return MetaModelParser.PACKAGE; }
					case -124:
						break;
					case 124:
						{ return MetaModelParser.PRIVATE; }
					case -125:
						break;
					case 125:
						{ return MetaModelParser.PROCESS; }
					case -126:
						break;
					case 126:
						{ return MetaModelParser.USEPORT; }
					case -127:
						break;
					case 127:
						{ return MetaModelParser.ABSTRACT; }
					case -128:
						break;
					case 128:
						{ return MetaModelParser.BLACKBOX; }
					case -129:
						break;
					case 129:
						{ return MetaModelParser.CONSTANT; }
					case -130:
						break;
					case 130:
						{ return MetaModelParser.CONTINUE; }
					case -131:
						break;
					case 131:
						{ return MetaModelParser.GETSCOPE; }
					case -132:
						break;
					case 132:
						{ return MetaModelParser.MAXDELTA; }
					case -133:
						break;
					case 133:
						{ return MetaModelParser.MINDELTA; }
					case -134:
						break;
					case 134:
						{ return MetaModelParser.PRIORITY; }
					case -135:
						break;
					case 135:
						{ return MetaModelParser.QUANTITY; }
					case -136:
						break;
					case 136:
						{ return MetaModelParser.SETSCOPE; }
					case -137:
						break;
					case 137:
						{ return MetaModelParser.TEMPLATE; }
					case -138:
						break;
					case 138:
						{ return MetaModelParser.ELABORATE; }
					case -139:
						break;
					case 139:
						{ return MetaModelParser.GETTHREAD; }
					case -140:
						break;
					case 140:
						{ return MetaModelParser.INTERFACE; }
					case -141:
						break;
					case 141:
						{ return MetaModelParser.PARAMETER; }
					case -142:
						break;
					case 142:
						{ return MetaModelParser.PROTECTED; }
					case -143:
						break;
					case 143:
						{ return MetaModelParser.SCHEDULER; }
					case -144:
						break;
					case 144:
						{ return MetaModelParser.CONSTRAINT; }
					case -145:
						break;
					case 145:
						{ return MetaModelParser.GETNTHPORT; }
					case -146:
						break;
					case 146:
						{ return MetaModelParser.GETPORTNUM; }
					case -147:
						break;
					case 147:
						{ return MetaModelParser.GETPROCESS; }
					case -148:
						break;
					case 148:
						{ return MetaModelParser.IMPLEMENTS; }
					case -149:
						break;
					case 149:
						{ return MetaModelParser.INSTANCEOF; }
					case -150:
						break;
					case 150:
						{ return MetaModelParser.BOUNDEDLOOP; }
					case -151:
						break;
					case 151:
						{ return MetaModelParser.GETCOMPNAME; }
					case -152:
						break;
					case 152:
						{ return MetaModelParser.GETINSTNAME; }
					case -153:
						break;
					case 153:
						{ return MetaModelParser.STATEMEDIUM; }
					case -154:
						break;
					case 154:
						{ return MetaModelParser.ADDCOMPONENT; }
					case -155:
						break;
					case 155:
						{ return MetaModelParser.GETCOMPONENT; }
					case -156:
						break;
					case 156:
						{ return MetaModelParser.REFINECONNECT; }
					case -157:
						break;
					case 157:
						{ return MetaModelParser.NONDETERMINISM; }
					case -158:
						break;
					case 158:
						{ return MetaModelParser.REDIRECTCONNECT; }
					case -159:
						break;
					case 159:
						{ return MetaModelParser.GETCONNECTIONNUM; }
					case -160:
						break;
					case 160:
						{ return MetaModelParser.GETCONNECTIONDEST; }
					case -161:
						break;
					case 161:
						{ return MetaModelParser.GETNTHCONNECTIONSRC; }
					case -162:
						break;
					case 162:
						{ return MetaModelParser.ISCONNECTIONREFINED; }
					case -163:
						break;
					case 163:
						{ return MetaModelParser.GETNTHCONNECTIONPORT; }
					case -164:
						break;
					case 165:
						{  throw new RuntimeException("Lexical error : Line " + 
                         (1 + yyline) + " Unmatched input: " + yytext()); }
					case -165:
						break;
					case 166:
						{ return yytext().charAt(0); }
					case -166:
						break;
					case 167:
						{ return yytext().charAt(0); }
					case -167:
						break;
					case 168:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.INT_LITERAL; }
					case -168:
						break;
					case 169:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -169:
						break;
					case 170:
						{ String s = yytext();
                  if (s.charAt(s.length() - 1) == ']') 
                      return MetaModelParser.EMPTY_DIM;
                  else 
                      return yytext().charAt(0); }
					case -170:
						break;
					case 171:
						{ String s = yytext();
                  s = s.substring(1,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.STRING_LITERAL; }
					case -171:
						break;
					case 172:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.DOUBLE_LITERAL; }
					case -172:
						break;
					case 173:
						{ String s = yytext();
                  s = s.substring(1,s.length() - 1);
                  returnVal = new MetaModelParserval(s);
	          return MetaModelParser.CHARACTER_LITERAL; }
					case -173:
						break;
					case 174:
						{ throw new RuntimeException("reserved keyword " + yytext() +
                        " ignored"); }
					case -174:
						break;
					case 176:
						{  throw new RuntimeException("Lexical error : Line " + 
                         (1 + yyline) + " Unmatched input: " + yytext()); }
					case -175:
						break;
					case 177:
						{ return yytext().charAt(0); }
					case -176:
						break;
					case 178:
						{ return yytext().charAt(0); }
					case -177:
						break;
					case 179:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -178:
						break;
					case 180:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.DOUBLE_LITERAL; }
					case -179:
						break;
					case 182:
						{  throw new RuntimeException("Lexical error : Line " + 
                         (1 + yyline) + " Unmatched input: " + yytext()); }
					case -180:
						break;
					case 183:
						{ return yytext().charAt(0); }
					case -181:
						break;
					case 184:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -182:
						break;
					case 185:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.DOUBLE_LITERAL; }
					case -183:
						break;
					case 187:
						{ return yytext().charAt(0); }
					case -184:
						break;
					case 188:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -185:
						break;
					case 190:
						{ return yytext().charAt(0); }
					case -186:
						break;
					case 191:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -187:
						break;
					case 193:
						{ return yytext().charAt(0); }
					case -188:
						break;
					case 194:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -189:
						break;
					case 196:
						{ return yytext().charAt(0); }
					case -190:
						break;
					case 197:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -191:
						break;
					case 199:
						{ return yytext().charAt(0); }
					case -192:
						break;
					case 200:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -193:
						break;
					case 202:
						{ return yytext().charAt(0); }
					case -194:
						break;
					case 203:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -195:
						break;
					case 205:
						{ return yytext().charAt(0); }
					case -196:
						break;
					case 206:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -197:
						break;
					case 208:
						{ return yytext().charAt(0); }
					case -198:
						break;
					case 209:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -199:
						break;
					case 211:
						{ return yytext().charAt(0); }
					case -200:
						break;
					case 212:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -201:
						break;
					case 214:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -202:
						break;
					case 216:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -203:
						break;
					case 218:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -204:
						break;
					case 220:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -205:
						break;
					case 222:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -206:
						break;
					case 224:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -207:
						break;
					case 226:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -208:
						break;
					case 228:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -209:
						break;
					case 230:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -210:
						break;
					case 232:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -211:
						break;
					case 234:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -212:
						break;
					case 235:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -213:
						break;
					case 236:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -214:
						break;
					case 237:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -215:
						break;
					case 238:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -216:
						break;
					case 239:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -217:
						break;
					case 240:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -218:
						break;
					case 241:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -219:
						break;
					case 242:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -220:
						break;
					case 243:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -221:
						break;
					case 244:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -222:
						break;
					case 245:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -223:
						break;
					case 246:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -224:
						break;
					case 247:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -225:
						break;
					case 248:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -226:
						break;
					case 249:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -227:
						break;
					case 250:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -228:
						break;
					case 251:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -229:
						break;
					case 252:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -230:
						break;
					case 253:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -231:
						break;
					case 254:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -232:
						break;
					case 255:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -233:
						break;
					case 256:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -234:
						break;
					case 257:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -235:
						break;
					case 258:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -236:
						break;
					case 259:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -237:
						break;
					case 260:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -238:
						break;
					case 261:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -239:
						break;
					case 262:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -240:
						break;
					case 263:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -241:
						break;
					case 264:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -242:
						break;
					case 265:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -243:
						break;
					case 266:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -244:
						break;
					case 267:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -245:
						break;
					case 268:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -246:
						break;
					case 269:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -247:
						break;
					case 270:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -248:
						break;
					case 271:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -249:
						break;
					case 272:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -250:
						break;
					case 273:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -251:
						break;
					case 274:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -252:
						break;
					case 275:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -253:
						break;
					case 276:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -254:
						break;
					case 277:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -255:
						break;
					case 278:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -256:
						break;
					case 279:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -257:
						break;
					case 280:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -258:
						break;
					case 281:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -259:
						break;
					case 282:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -260:
						break;
					case 283:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -261:
						break;
					case 284:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -262:
						break;
					case 285:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -263:
						break;
					case 286:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -264:
						break;
					case 287:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -265:
						break;
					case 288:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -266:
						break;
					case 289:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -267:
						break;
					case 290:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -268:
						break;
					case 291:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -269:
						break;
					case 292:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -270:
						break;
					case 293:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -271:
						break;
					case 294:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -272:
						break;
					case 295:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -273:
						break;
					case 296:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -274:
						break;
					case 297:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -275:
						break;
					case 298:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -276:
						break;
					case 299:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -277:
						break;
					case 300:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -278:
						break;
					case 301:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -279:
						break;
					case 302:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -280:
						break;
					case 303:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -281:
						break;
					case 304:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -282:
						break;
					case 305:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -283:
						break;
					case 306:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -284:
						break;
					case 307:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -285:
						break;
					case 308:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -286:
						break;
					case 309:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -287:
						break;
					case 310:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -288:
						break;
					case 311:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -289:
						break;
					case 312:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -290:
						break;
					case 313:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -291:
						break;
					case 314:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -292:
						break;
					case 315:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -293:
						break;
					case 316:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -294:
						break;
					case 317:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -295:
						break;
					case 318:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -296:
						break;
					case 319:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -297:
						break;
					case 320:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -298:
						break;
					case 321:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -299:
						break;
					case 322:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -300:
						break;
					case 323:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -301:
						break;
					case 324:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -302:
						break;
					case 325:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -303:
						break;
					case 326:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -304:
						break;
					case 327:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -305:
						break;
					case 328:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -306:
						break;
					case 329:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.DOUBLE_LITERAL; }
					case -307:
						break;
					case 330:
						{ throw new RuntimeException("reserved keyword " + yytext() +
                        " ignored"); }
					case -308:
						break;
					case 333:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -309:
						break;
					case 334:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -310:
						break;
					case 335:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -311:
						break;
					case 336:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -312:
						break;
					case 337:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -313:
						break;
					case 338:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -314:
						break;
					case 339:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -315:
						break;
					case 340:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -316:
						break;
					case 341:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -317:
						break;
					case 342:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -318:
						break;
					case 343:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -319:
						break;
					case 344:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -320:
						break;
					case 345:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -321:
						break;
					case 346:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -322:
						break;
					case 347:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -323:
						break;
					case 348:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -324:
						break;
					case 349:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -325:
						break;
					case 350:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -326:
						break;
					case 351:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -327:
						break;
					case 352:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -328:
						break;
					case 353:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -329:
						break;
					case 354:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -330:
						break;
					case 355:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -331:
						break;
					case 356:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -332:
						break;
					case 357:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -333:
						break;
					case 358:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -334:
						break;
					case 359:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -335:
						break;
					case 360:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -336:
						break;
					case 361:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -337:
						break;
					case 362:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -338:
						break;
					case 363:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -339:
						break;
					case 364:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -340:
						break;
					case 365:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -341:
						break;
					case 366:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -342:
						break;
					case 367:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -343:
						break;
					case 368:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -344:
						break;
					case 369:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -345:
						break;
					case 370:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -346:
						break;
					case 371:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -347:
						break;
					case 372:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -348:
						break;
					case 373:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -349:
						break;
					case 374:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -350:
						break;
					case 375:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -351:
						break;
					case 376:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -352:
						break;
					case 377:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -353:
						break;
					case 378:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -354:
						break;
					case 379:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -355:
						break;
					case 380:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -356:
						break;
					case 381:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -357:
						break;
					case 382:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -358:
						break;
					case 383:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -359:
						break;
					case 384:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -360:
						break;
					case 385:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -361:
						break;
					case 386:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -362:
						break;
					case 387:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -363:
						break;
					case 388:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -364:
						break;
					case 389:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -365:
						break;
					case 390:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -366:
						break;
					case 391:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -367:
						break;
					case 392:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -368:
						break;
					case 393:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -369:
						break;
					case 394:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -370:
						break;
					case 395:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -371:
						break;
					case 396:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -372:
						break;
					case 397:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -373:
						break;
					case 398:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -374:
						break;
					case 399:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -375:
						break;
					case 400:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -376:
						break;
					case 401:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -377:
						break;
					case 402:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -378:
						break;
					case 403:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -379:
						break;
					case 404:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -380:
						break;
					case 405:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -381:
						break;
					case 406:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -382:
						break;
					case 407:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -383:
						break;
					case 408:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -384:
						break;
					case 409:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -385:
						break;
					case 410:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -386:
						break;
					case 411:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -387:
						break;
					case 412:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -388:
						break;
					case 413:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -389:
						break;
					case 414:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -390:
						break;
					case 415:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -391:
						break;
					case 416:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -392:
						break;
					case 417:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -393:
						break;
					case 418:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -394:
						break;
					case 419:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -395:
						break;
					case 420:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -396:
						break;
					case 421:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -397:
						break;
					case 422:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -398:
						break;
					case 423:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -399:
						break;
					case 424:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -400:
						break;
					case 425:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -401:
						break;
					case 426:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -402:
						break;
					case 427:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -403:
						break;
					case 428:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -404:
						break;
					case 429:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -405:
						break;
					case 430:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -406:
						break;
					case 431:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -407:
						break;
					case 432:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -408:
						break;
					case 433:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -409:
						break;
					case 434:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -410:
						break;
					case 435:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -411:
						break;
					case 436:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -412:
						break;
					case 437:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -413:
						break;
					case 438:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -414:
						break;
					case 439:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -415:
						break;
					case 440:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -416:
						break;
					case 441:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -417:
						break;
					case 442:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -418:
						break;
					case 443:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -419:
						break;
					case 444:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -420:
						break;
					case 445:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -421:
						break;
					case 446:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -422:
						break;
					case 447:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -423:
						break;
					case 448:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -424:
						break;
					case 449:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -425:
						break;
					case 450:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -426:
						break;
					case 451:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -427:
						break;
					case 452:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -428:
						break;
					case 453:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -429:
						break;
					case 454:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -430:
						break;
					case 455:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -431:
						break;
					case 456:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -432:
						break;
					case 457:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -433:
						break;
					case 458:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -434:
						break;
					case 459:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -435:
						break;
					case 460:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -436:
						break;
					case 461:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -437:
						break;
					case 462:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -438:
						break;
					case 463:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -439:
						break;
					case 464:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -440:
						break;
					case 465:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -441:
						break;
					case 466:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -442:
						break;
					case 467:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -443:
						break;
					case 468:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -444:
						break;
					case 469:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -445:
						break;
					case 470:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -446:
						break;
					case 471:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -447:
						break;
					case 472:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -448:
						break;
					case 473:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -449:
						break;
					case 474:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -450:
						break;
					case 475:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -451:
						break;
					case 476:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -452:
						break;
					case 477:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -453:
						break;
					case 478:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -454:
						break;
					case 479:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -455:
						break;
					case 480:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -456:
						break;
					case 481:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -457:
						break;
					case 482:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -458:
						break;
					case 483:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -459:
						break;
					case 484:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -460:
						break;
					case 485:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -461:
						break;
					case 486:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -462:
						break;
					case 487:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -463:
						break;
					case 488:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -464:
						break;
					case 489:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -465:
						break;
					case 490:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -466:
						break;
					case 491:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -467:
						break;
					case 492:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -468:
						break;
					case 493:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -469:
						break;
					case 494:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -470:
						break;
					case 495:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -471:
						break;
					case 496:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -472:
						break;
					case 497:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -473:
						break;
					case 498:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -474:
						break;
					case 499:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -475:
						break;
					case 500:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -476:
						break;
					case 501:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -477:
						break;
					case 502:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -478:
						break;
					case 503:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -479:
						break;
					case 504:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -480:
						break;
					case 505:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -481:
						break;
					case 506:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -482:
						break;
					case 507:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -483:
						break;
					case 508:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -484:
						break;
					case 509:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -485:
						break;
					case 510:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -486:
						break;
					case 511:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -487:
						break;
					case 512:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -488:
						break;
					case 513:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -489:
						break;
					case 514:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -490:
						break;
					case 515:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -491:
						break;
					case 516:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -492:
						break;
					case 517:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -493:
						break;
					case 518:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -494:
						break;
					case 519:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -495:
						break;
					case 520:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -496:
						break;
					case 521:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -497:
						break;
					case 522:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -498:
						break;
					case 523:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -499:
						break;
					case 524:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -500:
						break;
					case 525:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -501:
						break;
					case 526:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -502:
						break;
					case 527:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -503:
						break;
					case 528:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -504:
						break;
					case 529:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -505:
						break;
					case 530:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -506:
						break;
					case 531:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -507:
						break;
					case 532:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -508:
						break;
					case 533:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -509:
						break;
					case 534:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -510:
						break;
					case 535:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -511:
						break;
					case 536:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -512:
						break;
					case 537:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -513:
						break;
					case 538:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -514:
						break;
					case 539:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -515:
						break;
					case 540:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -516:
						break;
					case 541:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -517:
						break;
					case 542:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -518:
						break;
					case 543:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -519:
						break;
					case 544:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -520:
						break;
					case 545:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -521:
						break;
					case 546:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -522:
						break;
					case 547:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -523:
						break;
					case 548:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -524:
						break;
					case 549:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -525:
						break;
					case 550:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -526:
						break;
					case 551:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -527:
						break;
					case 552:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -528:
						break;
					case 553:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -529:
						break;
					case 554:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -530:
						break;
					case 555:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -531:
						break;
					case 556:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -532:
						break;
					case 557:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -533:
						break;
					case 558:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -534:
						break;
					case 559:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -535:
						break;
					case 560:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -536:
						break;
					case 561:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -537:
						break;
					case 562:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -538:
						break;
					case 563:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -539:
						break;
					case 564:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -540:
						break;
					case 565:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -541:
						break;
					case 566:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -542:
						break;
					case 567:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -543:
						break;
					case 568:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -544:
						break;
					case 569:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -545:
						break;
					case 570:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -546:
						break;
					case 571:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -547:
						break;
					case 572:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -548:
						break;
					case 573:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -549:
						break;
					case 574:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -550:
						break;
					case 575:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -551:
						break;
					case 576:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -552:
						break;
					case 577:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -553:
						break;
					case 578:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -554:
						break;
					case 579:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -555:
						break;
					case 580:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -556:
						break;
					case 581:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -557:
						break;
					case 582:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -558:
						break;
					case 583:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -559:
						break;
					case 584:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -560:
						break;
					case 585:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -561:
						break;
					case 586:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -562:
						break;
					case 587:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -563:
						break;
					case 588:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -564:
						break;
					case 589:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -565:
						break;
					case 590:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -566:
						break;
					case 591:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -567:
						break;
					case 592:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -568:
						break;
					case 593:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -569:
						break;
					case 594:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -570:
						break;
					case 595:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -571:
						break;
					case 596:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -572:
						break;
					case 597:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -573:
						break;
					case 598:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -574:
						break;
					case 599:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -575:
						break;
					case 600:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -576:
						break;
					case 601:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -577:
						break;
					case 602:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -578:
						break;
					case 603:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -579:
						break;
					case 604:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -580:
						break;
					case 605:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -581:
						break;
					case 606:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -582:
						break;
					case 607:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -583:
						break;
					case 608:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -584:
						break;
					case 609:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -585:
						break;
					case 610:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -586:
						break;
					case 611:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -587:
						break;
					case 612:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -588:
						break;
					case 613:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -589:
						break;
					case 614:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -590:
						break;
					case 615:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -591:
						break;
					case 616:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -592:
						break;
					case 617:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -593:
						break;
					case 618:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -594:
						break;
					case 619:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -595:
						break;
					case 620:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -596:
						break;
					case 621:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -597:
						break;
					case 622:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -598:
						break;
					case 623:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -599:
						break;
					case 624:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -600:
						break;
					case 625:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -601:
						break;
					case 626:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -602:
						break;
					case 627:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -603:
						break;
					case 628:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -604:
						break;
					case 629:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -605:
						break;
					case 630:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -606:
						break;
					case 631:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -607:
						break;
					case 632:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -608:
						break;
					case 633:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -609:
						break;
					case 634:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -610:
						break;
					case 635:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -611:
						break;
					case 636:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -612:
						break;
					case 637:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -613:
						break;
					case 638:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -614:
						break;
					case 639:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -615:
						break;
					case 640:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -616:
						break;
					case 641:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -617:
						break;
					case 642:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -618:
						break;
					case 643:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -619:
						break;
					case 644:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -620:
						break;
					case 645:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -621:
						break;
					case 646:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -622:
						break;
					case 647:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -623:
						break;
					case 648:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -624:
						break;
					case 649:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -625:
						break;
					case 650:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -626:
						break;
					case 651:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -627:
						break;
					case 652:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -628:
						break;
					case 653:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -629:
						break;
					case 654:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -630:
						break;
					case 655:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -631:
						break;
					case 656:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -632:
						break;
					case 657:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -633:
						break;
					case 658:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -634:
						break;
					case 659:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -635:
						break;
					case 660:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -636:
						break;
					case 661:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -637:
						break;
					case 662:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -638:
						break;
					case 663:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -639:
						break;
					case 664:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -640:
						break;
					case 665:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -641:
						break;
					case 666:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -642:
						break;
					case 667:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -643:
						break;
					case 668:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -644:
						break;
					case 669:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -645:
						break;
					case 670:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -646:
						break;
					case 671:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -647:
						break;
					case 672:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -648:
						break;
					case 673:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -649:
						break;
					case 674:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -650:
						break;
					case 675:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -651:
						break;
					case 676:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -652:
						break;
					case 677:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -653:
						break;
					case 678:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -654:
						break;
					case 679:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -655:
						break;
					case 680:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -656:
						break;
					case 681:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -657:
						break;
					case 682:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -658:
						break;
					case 683:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -659:
						break;
					case 684:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -660:
						break;
					case 685:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -661:
						break;
					case 686:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -662:
						break;
					case 687:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -663:
						break;
					case 688:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -664:
						break;
					case 689:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -665:
						break;
					case 690:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -666:
						break;
					case 691:
						{ returnVal = new MetaModelParserval(yytext());
                  return MetaModelParser.IDENTIFIER; }
					case -667:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
					}
				}
			}
		}
	}
}
