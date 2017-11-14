package org.underworldlabs.swing.hexeditor.bdoc;

public class ByteSpan {

  // PRIVATE MEMBERS
  private Location startLocation;
  private Location endLocation;

  // CONSTRUCTORS
  public ByteSpan( Location startLocation, Location endLocation ) {
  
    if ( startLocation.getDocument() != endLocation.getDocument() )
      throw new DocumentMismatchException(startLocation.getDocument(),
        "Bytespan start and end positions must belong to the same binary document.");
              
    this.startLocation = startLocation;
    this.endLocation   = endLocation;
  }

  // GETTERS
  public Location getStartLocation() {
    return startLocation;
  }

  public Location getEndLocation() {
    return endLocation;
  }

  public BinaryDocument getDocument() {
    return startLocation.getDocument();
  }

  public long length() {
    return endLocation.getOffset() -
           startLocation.getOffset() + 1;
  }

  public boolean contains( Location loc ) {
    return loc.compareTo(startLocation) >= 0 &&
           loc.compareTo(endLocation) <= 0;
  }

  public boolean contains( ByteSpan span ) {
    return startLocation.compareTo( span.getStartLocation() ) <= 0 &&
           endLocation.compareTo( span.getEndLocation() ) >= 0;
  }

  public ByteSpan [] union( ByteSpan span ) {
    ByteSpan [] result = null;
    
    if (span == null) {
      result = new ByteSpan[1]; 
      result[0] = this;
      return result;
    }

    Location outerStart, innerStart, innerEnd, outerEnd;

    if (startLocation.compareTo(span.getStartLocation()) < 0) {
      outerStart = startLocation;
      innerStart = span.getStartLocation();
    }
    else {
      outerStart = span.getStartLocation();
      innerStart = startLocation;
    }

    if (endLocation.compareTo(span.getEndLocation()) < 0) {
      innerEnd = endLocation;
      outerEnd = span.getEndLocation();
    }
    else {
      innerEnd = span.getEndLocation();
      outerEnd = endLocation;
    } 

    if ( innerEnd.compareTo(innerStart) < 0 ) {
      result = new ByteSpan[2];
      result[0] = new ByteSpan( outerStart, innerEnd );
      result[1] = new ByteSpan( innerStart, outerEnd );
    }
    else {
      result = new ByteSpan[1];
      result[0] = new ByteSpan( outerStart, outerEnd );
    }
    
    return result;
  }

  public ByteSpan intersection( ByteSpan span ) {
    if (span == null)
      return null;
    
    Location s,e;
    
    if ( startLocation.compareTo( span.getStartLocation() ) < 0 )
      s = span.getStartLocation();
    else
      s = startLocation;
    
    if ( endLocation.compareTo( span.getEndLocation() ) < 0 )
      e = endLocation;
    else
      e = getEndLocation();
    
    return new ByteSpan( s, e );
  }

  public ByteSpan [] difference( ByteSpan span ) {
    ByteSpan [] result;

    if (span == null) {
      result = new ByteSpan[1];
      result[0] = this;
      return result;
    }
      
    if (contains(span)) {
      result = new ByteSpan[2];
      result[0] = new ByteSpan( startLocation, span.getStartLocation().addOffset(-1) );
      result[1] = new ByteSpan( span.getEndLocation().addOffset(1), endLocation );
    }
    else {
      result = new ByteSpan[1];
      if (startLocation.compareTo(span.getStartLocation()) < 0)
        result[0] = new ByteSpan(startLocation, span.getStartLocation().addOffset(-1));
      else
        result[0] = new ByteSpan(span.getEndLocation().addOffset(1), endLocation);
    }

    return result;
  }
	
  public String toString() {
    return "ByteSpan[start=" + getStartLocation().getOffset() + ", end=" + getEndLocation().getOffset() + ", length=" + length() + "]";
  }
}
