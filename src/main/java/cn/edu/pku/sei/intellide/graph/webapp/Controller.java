package cn.edu.pku.sei.intellide.graph.webapp;

import cn.edu.pku.sei.intellide.graph.qa.code_search.CodeSearch;
import cn.edu.pku.sei.intellide.graph.qa.doc_search.DocSearch;
import cn.edu.pku.sei.intellide.graph.qa.nl_query.NLQueryEngine;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Conf;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jNode;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jRelation;
import cn.edu.pku.sei.intellide.graph.webapp.entity.Neo4jSubGraph;
import org.apache.lucene.queryparser.classic.ParseException;
import org.neo4j.cypher.export.SubGraph;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

@CrossOrigin
@RestController
public class Controller {

    @Autowired
    private Context context;

    CodeSearch codeSearch=null;
    DocSearch docSearch=null;
    NLQueryEngine nlQueryEngine=null;

    @RequestMapping(value = "/codeSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jSubGraph codeSearch(@RequestParam(value="query", defaultValue="") String query){
        if (nlQueryEngine==null)
            nlQueryEngine=new NLQueryEngine(context.db,context.dataDir);
        if (codeSearch==null)
            codeSearch=new CodeSearch(context.db);
        nlQueryEngine.createIndex();
        Neo4jSubGraph r=nlQueryEngine.search(query);
        if (r.getNodes().size()>0)
            return r;
        return codeSearch.search(query);
    }

    @RequestMapping(value = "/docSearch", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jNode> docSearch(@RequestParam(value="query", defaultValue="") String query) throws IOException, ParseException {
        codeSearch("");
        if (docSearch==null)
            docSearch=new DocSearch(context.db,context.dataDir+"/doc_search_index", codeSearch);
        return docSearch.search(query);
    }

    @RequestMapping(value = "/relationList", method = {RequestMethod.GET,RequestMethod.POST})
    public List<Neo4jRelation> relationList(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jRelation.getNeo4jRelationList(id,context.db);
    }

    @RequestMapping(value = "/node", method = {RequestMethod.GET,RequestMethod.POST})
    public Neo4jNode node(@RequestParam(value="id", defaultValue="") long id){
        return Neo4jNode.get(id,context.db);
    }

}

@Component
class Context{

    GraphDatabaseService db=null;
    String dataDir=null;

    @Autowired
    public Context(Conf conf){
        this.dataDir=conf.getDataDir();
        this.db= new GraphDatabaseFactory().newEmbeddedDatabase(new File(conf.getGraphDir()));
    }

}