package utils;



import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class sits atop Collection and uses ArrayList to create a collection of Tuples.
 * The motivation behind this is that we were using a Map to implement this, but it's
 * totally legitimate to have tuples with the same key, in fact, we were doing that
 * ourselves. That Map couldn't support this didn't really scream at us until testing
 * demonstrated it, a "Doh!" sort of moment.
 *
 * Therefore, this class is nothing more than a list of Tuples. There
 * may be duplicates, even duplicate identical by both their key and their value.
 *
 * @author Thomas Greger and Russell Bateman
 * @since August 2012
 */
public class TupleList extends XmlAdapter< Element, TupleList >
                         implements Collection< Tuple< String, String > >, Serializable
{
    private ArrayList< Tuple< String, String > > list = new ArrayList< Tuple< String, String > >();

    public TupleList() { }

    /**
     * This constructor names the field when serializing. Typically, it's called by a subclass and
     * the whole TupleList class is never used directly.
     * @param tagname
     */
    public TupleList( String tagname )
    {
        this.tagname = tagname;
    }

    /**
     * This is a special constructor created as shorthand for the JaxB serializer to use.
     * @param list array of tuples.
     */
    public TupleList( ArrayList< Tuple< String, String > > list )
    {
        addAll( list );
    }

    @Override
    public int size() { return this.list.size(); }

    @Override
    public boolean isEmpty() { return this.list.size() == 0; }

    @Override
    public boolean contains( Object o )
    {
        return list.contains( o );
    }

    /**
     * Determine whether the [ key, value ] pair are present in the list.
     *
     * @param key the key string.
     * @param value the value string.
     * @return true/false that the tuple is present.
     */
    public boolean contains( String key, String value )
    {
        for( Tuple< String, String > v : list )
        {
            if(  v.getKey().equals( key ) && v.getValue().equals( value ) )
              return true;
        }

        return false;
    }

    /**
     * Determine whether a tuple with the specified key is present in the list.
     *
     * @param key the key string.
     * @return true/false that the key is present.
     */
    public boolean contains( String key )
    {
        for( Tuple< String, String > v : list )
        {
            if(  v.getKey().equals( key ) )
              return true;
        }

        return false;
    }

    /**
     * Determine whether the [ key, value ] pair in the passed tuple are present
     * in the list.
     *
     * @param tuple containing the key and value to match.
     * @return true/false that the tuple is present.
     */
    public boolean contains( Tuple< String, String > tuple )
    {
        String key   = tuple.getKey();
        String value = tuple.getValue();

        return contains( key, value );
    }

    @Override
    public Iterator< Tuple< String, String > > iterator()
    {
        return list.iterator();
    }

    @Override
    public Object[] toArray()
    {
        return list.toArray();
    }

    @Override
    public < T > T[] toArray( T[] a )
    {
        return list.toArray( a );
    }

    /**
     * This is to overcome the difficulty I have working with the two preceding methods.
     * @return a copy of our array of tuples.
     */
    public ArrayList< Tuple< String, String > > toTupleArray()
    {
        ArrayList< Tuple< String, String > > array = new ArrayList< Tuple< String, String > >( this.size() );

        for( Tuple< String, String > tuple : list )
            array.add( new Tuple< String, String >( tuple ) );

        return array;
    }

    /**
     * Add a new tuple to the list.
     * @param [ key, value ] pair to add.
     * @return true/false that it was added.
     */
    @Override
    public boolean add( Tuple< String, String > e )
    {
        return list.add( e );
    }

    /**
     * Add a new tuple [ key, value ] pair to the list.
     * @param key the key string.
     * @param value the value string.
     * @return true/false that it was added.
     */
    public boolean add( String key, String value )
    {
        Tuple< String, String > tuple = new Tuple< String, String >( key, value );

        return add( tuple );
    }

    /**
     * Remove an existing tuple [ key, value ] pair to the list.
     * @param key the key string.
     * @param value the value string.
     * @return true/false that it was added.
     */
    @Override
    public boolean remove( Object o )
    {
        if( !( o instanceof Tuple ) )
            return false;

        @SuppressWarnings( "unchecked" )
        Tuple< String, String > t = ( Tuple< String, String > ) o;

        return list.remove( t );
    }

    /**
     * Remove an existing tuple [ key, value ] pair to the list.
     * @param key the key string.
     * @param value the value string.
     * @return true/false that it was added.
     */
    public boolean remove( String key, String value )
    {
        Tuple< String, String > tuple = new Tuple< String, String >( key, value );

        return list.remove( tuple );
    }

    @Override
    public boolean containsAll( Collection< ? > c )
    {
        return list.containsAll( c );
    }

    @Override
    public boolean addAll( Collection< ? extends Tuple< String, String > > c )
    {
        return list.addAll( c );
    }

    @Override
    public boolean removeAll( Collection< ? > c )
    {
        return list.removeAll( c );
    }

    @Override
    public boolean retainAll( Collection< ? > c )
    {
        return list.retainAll( c );
    }

    @Override
    public void clear()
    {
        list.clear();
    }

    public boolean equals( TupleList tuplelist )
    {
        if( tuplelist == this )
            return true;

        if( tuplelist.size() != this.size() )
            return false;

        // find every tuple in this object in the tuplelist passed or else!
        for( Tuple< String, String > tuple : list )
        {
            boolean found = false;

            ArrayList< Tuple< String, String > > thatlist = tuplelist.toTupleArray();

            for( Tuple< String, String > that : thatlist )
            {
                if( that.equals( tuple ) )
                    found = true;
            }

            if( !found )
                return false;
        }

        return true;
    }

    public String toString()
    {
        if( this.size() < 1 )
            return "";

        StringBuffer buffer = new StringBuffer( this.size() * 7 * 3 );

        buffer.append( "{<n" );

        int count = this.size();

        for( Tuple< String, String > tuple : list )
        {
            buffer.append( "  \"" + tuple.getKey() + "\"" );
            buffer.append( ":\""  + tuple.getValue() + "\"" );

            if( count -- > 1 )
                buffer.append( "," );

            buffer.append( "\n" );
        }

        buffer.append( "}" );

        return buffer.toString();
    }

    /**
     * Returns all tuples that with the indicated key. The point to this class in
     * the first place is that there can be any tuples including tuples with the same
     * key or even the same key and value.
     * @param key to search for.
     * @return list of matching tuples.
     */
    public ArrayList< Tuple< String, String > > getAll( String key )
    {
        ArrayList< Tuple< String, String > > list = new ArrayList< Tuple< String, String > >();

        for( Tuple< String, String > tuple : this.list )
        {
            if( tuple.key.equals( key ) )
                list.add( tuple );
        }

        return list;
    }

    /**
     * This returns the first tuple found with the specified key. There may be others,
     * but there is no order maintained so which value comes back cannot be predicted.
     * @param key to search for.
     * @return value associated with the key found.
     */
    public String getFirst( String key )
    {
        ArrayList< Tuple< String, String > > list = getAll( key );

        if( list == null || list.size() < 1 )
            return null;

        return list.get(  0 ).getValue();
    }

    /* ========== J A X B   s e r i a l i z a t i o n   c o d e ========================================
     * This enables us to make use of TupleList through Jersey. Originally, I got this code
     * for using Map/HashMap from Blaise Doughan, team lead for EclipseLink; see
     * http://stackoverflow.com/questions/11353790/serializer-for-hashmaps-for-jersey-use.
     */
    private static boolean  initDocumentBuilder = false;
    private DocumentBuilder builder = null;
    private String          tagname = "tuplelist";   // generic name; subclass this

    public final String getTagname() { return this.tagname; }

    private void initDocumentBuilder()
    {
        try
        {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            initDocumentBuilder = true;
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public Element marshal( TupleList tuplelist ) throws Exception
    {
        if( !initDocumentBuilder )
            initDocumentBuilder();

        Document document    = builder.newDocument();
        Element  rootElement = document.createElement( tagname );

        document.appendChild( rootElement );

        ArrayList< Tuple< String, String > > list = tuplelist.toTupleArray();

        for( Tuple< String, String > tuple : list )
        {
            Element childElement = document.createElement( tuple.getKey() );

            childElement.setTextContent( tuple.getValue() );
            rootElement.appendChild( childElement );
        }

        return rootElement;
    }

    @Override
    public TupleList unmarshal( Element rootElement ) throws Exception
    {
        NodeList                             nodeList = rootElement.getChildNodes();
        ArrayList< Tuple< String, String > > list
                        = new ArrayList< Tuple< String, String > >( nodeList.getLength() );

        /* Pre-allocate the array we'll use in the TupleList; fill it using the child nodes
         * of the in-coming Element.
         */
        for( int x = 0; x < nodeList.getLength(); x++ )
        {
            Node node = nodeList.item( x );

            if( node.getNodeType() == Node.ELEMENT_NODE )
                list.add( new Tuple< String, String >( node.getNodeName(), node.getTextContent() ) );
        }

        this.list = list;
        return this;
    }
}
