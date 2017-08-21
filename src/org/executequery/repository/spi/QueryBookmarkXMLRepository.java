/*
 * QueryBookmarkXMLRepository.java
 *
 * Copyright (C) 2002-2015 Takis Diakoumis
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.executequery.repository.spi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.executequery.repository.QueryBookmark;
import org.executequery.repository.QueryBookmarkRepository;
import org.executequery.repository.RepositoryException;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.MiscUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author   Takis Diakoumis
 * @version  $Revision: 1766 $
 * @date     $Date: 2017-08-14 23:34:37 +1000 (Mon, 14 Aug 2017) $
 */
public final class QueryBookmarkXMLRepository extends AbstractXMLResourceReaderWriter<QueryBookmark> 
    implements QueryBookmarkRepository {
    
    // -------------------------------------------
    // XML tag names and attributes
    
    private static final String FILE_PATH = "querybookmarks.xml";
    
    private static final String QUERY_BOOKMARKS = "query-bookmarks";
    private static final String BOOKMARK = "query-bookmark";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ORDER = "order";
    
    public QueryBookmarkXMLRepository() {}

    public void save(List<QueryBookmark> bookmarks) 
        throws RepositoryException {
        write(filePath(), new QueryBookmarkParser(), 
                new QueryBookmarkInputSource(bookmarks));
    }

    public List<QueryBookmark> open() {
        
        return (List<QueryBookmark>)read(filePath(), new QueryBookmarkHandler());
    }
    
    private String filePath() {
        
        UserSettingsProperties settings = new UserSettingsProperties();
        
        return settings.getUserSettingsDirectory() + FILE_PATH;
    }
    
    public String getId() {

        return REPOSITORY_ID;
    }
    
    class QueryBookmarkHandler 
        extends AbstractXMLRepositoryHandler<QueryBookmark> {

        private List<QueryBookmark> bookmarks;

        private QueryBookmark bookmark;
        
        QueryBookmarkHandler() {
            bookmarks = new ArrayList<QueryBookmark>();
        }

        public void startElement(String nameSpaceURI, String localName,
                                 String qName, Attributes attrs) {

            contents().reset();
            
            if (localNameIsKey(localName, BOOKMARK)) {

                bookmark = new QueryBookmark();
                bookmark.setId(attrs.getValue(ID));
                bookmark.setName(attrs.getValue(NAME));
                bookmark.setOrder(Integer.valueOf(attrs.getValue(ORDER)));
            }

        }
        
        public void endElement(String nameSpaceURI, String localName,
                               String qName) {

            if (localNameIsKey(localName, BOOKMARK)) {

                bookmark.setQuery(contentsAsString());
                bookmarks.add(bookmark);
                bookmark = null;
            }

        }

        public List<QueryBookmark> getRepositoryItemsList() {

            return bookmarks;
        }

    } // QueryBookmarkHandler
    
    class QueryBookmarkInputSource extends InputSource {
        
        private List<QueryBookmark> bookmarks;

        public QueryBookmarkInputSource(List<QueryBookmark> bookmarks) {

            super();
            this.bookmarks = bookmarks;
        }
        
        public List<QueryBookmark> getQueryBookmarks() {
            
            return bookmarks;
        }
        
    } // class QueryBookmarkInputSource
    
    class QueryBookmarkParser extends AbstractXMLRepositoryParser {

        public QueryBookmarkParser() {}

        public void parse(InputSource input) throws SAXException, IOException {

            if (!(input instanceof QueryBookmarkInputSource)) {

                throw new SAXException(
                        "Parser can only accept a QueryBookmarkInputSource");
            }
            
            parse((QueryBookmarkInputSource)input);
        }
        
        public void parse(QueryBookmarkInputSource input) 
            throws IOException, SAXException {

            validateHandler();
            
            List<QueryBookmark> bookmarks = input.getQueryBookmarks();
            
            handler().startDocument();
            newLine();
            handler().startElement(NSU, QUERY_BOOKMARKS, 
                    QUERY_BOOKMARKS, attributes());
            newLine();

            if (bookmarks != null) {

                writeXMLRows(bookmarks);
            }
            
            newLine();
            
            handler().endElement(NSU, QUERY_BOOKMARKS, QUERY_BOOKMARKS);
            handler().endDocument();

        }

        private void writeXMLRows(List<QueryBookmark> bookmarks)
            throws SAXException {

            for (QueryBookmark bookmark : bookmarks) {

                if (bookmark.isNew()) {

                    bookmark.setId(generateUniqueId());
                }
                
                newLineIndentOne();

                attributes().addAttribute(NSU, ID, ID, 
                        CDDATA, bookmark.getId());

                attributes().addAttribute(NSU, NAME, NAME, 
                        CDDATA, bookmark.getName());

                attributes().addAttribute(NSU, ORDER, ORDER, 
                        CDDATA, String.valueOf(bookmark.getOrder()));

                handler().startElement(NSU, BOOKMARK, BOOKMARK, attributes());
                newLine();

                resetAttributes();

                String query = bookmark.getQuery();
                handler().characters(query.toCharArray(), 0, query.length());
                
                newLineIndentOne();
                handler().endElement(NSU, BOOKMARK, BOOKMARK);
                newLine();
            }

        }

        private String generateUniqueId() {
            return MiscUtils.generateUniqueId();
        }
        
    } // class QueryBookmarkParser
    
}










