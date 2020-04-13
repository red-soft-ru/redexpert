package org.executequery.gui.browser;

import org.executequery.GUIUtilities;
import org.executequery.event.ApplicationEvent;
import org.executequery.event.ConnectionEvent;
import org.executequery.event.ConnectionListener;
import org.executequery.gui.ComponentPanel;
import org.executequery.gui.browser.nodes.DatabaseObjectNode;
import org.executequery.gui.browser.tree.SchemaTree;
import org.executequery.gui.forms.FormObjectView;
import org.executequery.repository.RepositoryException;
import org.executequery.repository.spi.AbstractXMLRepositoryHandler;
import org.executequery.repository.spi.AbstractXMLRepositoryParser;
import org.executequery.repository.spi.AbstractXMLResourceReaderWriter;
import org.executequery.util.UserSettingsProperties;
import org.underworldlabs.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConnectionHistory extends AbstractXMLResourceReaderWriter<String[]> implements ConnectionListener {

  private static final String FILE_PATH = "ConnectionHistory.xml";

  static ConnectionHistory connectionHistory;

  public static ConnectionHistory getInstance() {
    if (connectionHistory == null)
      connectionHistory = new ConnectionHistory();
    return connectionHistory;
  }

  private static List<String[]> listPaths;

  public static List<String[]> getListPaths() {
    if (listPaths == null)
      readListPaths();
    return listPaths;
  }

  public static void setListPaths(List<String[]> listPathss) {
    listPaths = listPathss;
    getInstance().save();
  }

  private static void readListPaths() {
    listPaths = getInstance().read();
  }

  public static void add(FormObjectView formObjectView) {
    if (formObjectView != null) {
      Vector<String> path = new Vector<>();
      DatabaseObjectNode don = formObjectView.getDatabaseObjectNode();
      TreeNode[] x = don.getPath();
      for (TreeNode node : x) {
        DatabaseObjectNode dnode = (DatabaseObjectNode) node;
        path.add(dnode.getName());
      }
      getListPaths().add(path.toArray(new String[path.size()]));
      getInstance().save();
    }
  }

  public static void remove(FormObjectView formObjectView) {
    if (formObjectView != null) {
      Vector<String> path = new Vector<>();
      DatabaseObjectNode don = formObjectView.getDatabaseObjectNode();
      if (don != null) {
        TreeNode[] mas = don.getPath();
        for (TreeNode node : mas) {
          DatabaseObjectNode dnode = (DatabaseObjectNode) node;
          path.add(dnode.getName());
        }
        String[] x = path.toArray(new String[path.size()]);
        for (int i = 0; i < getListPaths().size(); i++) {
          String[] y = getListPaths().get(i);
          if (cley(y, "^").equals(cley(x, "^"))) {
            getListPaths().remove(i);
            break;
          }
        }
        getInstance().save();
      }
    }
  }

  static String cley(String[] mas, String delimiter) {
    String result = "";
    for (int i = 0; i < mas.length; i++) {
      if (i == 0)
        result += mas[i];
      else result += delimiter + mas[i];
    }
    return result;
  }

  public List<String[]> read() {
    ensureFileExists();
    return read(filePath(), new ConnectionHistoryXMLHandler());
  }

  private static void ensureFileExists() {

    File file = new File(filePath());
    if (!file.exists()) {

      try {

        FileUtils.copyResource("org/executequery/connection-history-default.xml", filePath());

      } catch (IOException e) {

        throw new RepositoryException(e);
      }

    }

  }

  public void save() {
    write(filePath(), new ConnectionHistoryParser(), new PathInputSource(getListPaths()));
  }

  private static String filePath() {

    UserSettingsProperties settings = new UserSettingsProperties();
    return settings.getUserSettingsDirectory() + FILE_PATH;
  }

  @Override
  public void connected(ConnectionEvent connectionEvent) {
    ConnectionsTreePanel panel = (ConnectionsTreePanel) GUIUtilities.getDockedTabComponent(ConnectionsTreePanel.PROPERTY_KEY);
    SchemaTree tree = panel.getTree();
    int n = getListPaths().size();

    for (int i = 0; i < n; i++) {
      DatabaseObjectNode node = (DatabaseObjectNode) tree.getRootNode();
      String[] mas = getListPaths().get(i);
      boolean equal = true;
      for (int g = 0; g < mas.length; g++) {
        if (mas[g].equals(node.getName())) {
          if (node.isHostNode()) {
            String nodeName = node.getName();
            String connectionName = connectionEvent.getDatabaseConnection().getName();
            if (!nodeName.equals(connectionName)) {
              equal = false;
              break;
            }
          }
          if (g < mas.length - 1) {
            node.populateChildren();
            Enumeration<TreeNode> childs = node.children();
            if (!childs.hasMoreElements()) {
              equal = false;
            } else
              for (; childs.hasMoreElements(); ) {
                DatabaseObjectNode x = (DatabaseObjectNode)childs.nextElement();
                if (x.getName().equals(mas[g + 1])) {
                  node = x;
                  break;
                }
              }
          }
        } else {
          equal = false;
          break;
        }
      }
      if (equal) {
        getListPaths().remove(i);
        getInstance().save();
        i--;
        n--;
        panel.valueChanged(node, connectionEvent.getDatabaseConnection());
      }
    }
  }

  @Override
  public void disconnected(ConnectionEvent connectionEvent) {
    List<ComponentPanel> panels = GUIUtilities.getOpenPanels();
    Vector<String> closeTabs = new Vector();
    for (int i = 0; i < panels.size(); i++) {
      if (panels.get(i).getComponent() instanceof BrowserViewPanel) {
        BrowserViewPanel panel = (BrowserViewPanel) panels.get(i).getComponent();
        if (panel.getCurrentView() != null && !panels.get(i).getName().equals(BrowserViewPanel.TITLE))
          if (panel.getCurrentView().getDatabaseConnection() == connectionEvent.getDatabaseConnection())
            closeTabs.add(panels.get(i).getName());
      }
    }
    List<String[]> copy = new ArrayList<>();
    copy.addAll(getListPaths());
    for (String name : closeTabs)
      GUIUtilities.closeTab(name);
    setListPaths(copy);
  }

  @Override
  public boolean canHandleEvent(ApplicationEvent event) {
    return true;
  }

  public class ConnectionHistoryXMLHandler extends AbstractXMLRepositoryHandler<String[]> {

    private List<String[]> paths;

    private Vector<String> path;

    private Properties advancedProperties;

    public ConnectionHistoryXMLHandler() {

      paths = new Vector<String[]>();
      path = new Vector<>();
    }

    public void startElement(String nameSpaceURI, String localName,
                             String qName, Attributes attrs) {
      contents().reset();
    }

    public void endElement(String nameSpaceURI, String localName, String qName) {
      if (localName == "path")
        if (path.size() > 0) {
          paths.add(path.toArray(new String[path.size()]));
          path = new Vector<>();
        }
      if (localName == "node")
        path.add(contentsAsString());

    }

    @Override
    public List<String[]> getRepositoryItemsList() {
      return paths;
    }
  }

  public class ConnectionHistoryParser extends AbstractXMLRepositoryParser {
    ConnectionHistoryParser() {
      super();
    }

    @Override
    public void parse(InputSource inputSource) throws IOException, SAXException {
      if (inputSource instanceof PathInputSource) {
        PathInputSource source = (PathInputSource) inputSource;
        handler().startDocument();
        newLine();
        handler().startElement("", "paths", "paths", attributes());
        newLine();

        for (String[] path : source.getPaths()) {
          handler().startElement("", "path", "path", attributes());
          newLine();
          for (String node : path) {
            writeXML("node", node, "\n      ");
          }
          newLine();
          handler().endElement("", "path", "path");

        }
        newLine();
        handler().endElement("", "paths", "paths");
        handler().endDocument();
      }
    }
  }

  class PathInputSource extends InputSource {

    private List<String[]> paths;

    public PathInputSource(List<String[]> paths) {

      super();
      this.paths = paths;
    }

    public List<String[]> getPaths() {

      return paths;
    }

  }
}
