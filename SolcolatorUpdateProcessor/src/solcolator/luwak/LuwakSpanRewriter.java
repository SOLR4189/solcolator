package solcolator.luwak;

import java.io.IOException;
 
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SynonymQuery;
import org.apache.lucene.search.TermInSetQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.spans.SpanQuery;

import uk.co.flax.luwak.util.ForceNoBulkScoringQuery;
import uk.co.flax.luwak.util.RewriteException;
import uk.co.flax.luwak.util.SpanOffsetReportingQuery;
import uk.co.flax.luwak.util.SpanRewriter;


public class LuwakSpanRewriter extends SpanRewriter {
	public static final LuwakSpanRewriter INSTANCE = new LuwakSpanRewriter();
	
	@Override
	public Query rewrite(Query in, IndexSearcher searcher) throws RewriteException, IOException {
		if (in instanceof SynonymQuery) {
			return rewriteSynonymQuery((SynonymQuery)in, searcher);
		}
		/*if (in instanceof MatchAllDocsQuery) {
			return new TermQuery(new Term("item_id:*"));
		}*/
		if (in instanceof SpanOffsetReportingQuery)
            return in;
        if (in instanceof SpanQuery)
            return forceOffsets((SpanQuery)in);
        if (in instanceof ForceNoBulkScoringQuery) {
            return new ForceNoBulkScoringQuery(rewrite(((ForceNoBulkScoringQuery) in).getWrappedQuery(), searcher));
        }
        if (in instanceof TermQuery)
            return rewriteTermQuery((TermQuery)in);
        if (in instanceof BooleanQuery)
            return rewriteBoolean((BooleanQuery) in, searcher);
        if (in instanceof MultiTermQuery)
            return rewriteMultiTermQuery((MultiTermQuery)in);
        if (in instanceof DisjunctionMaxQuery)
            return rewriteDisjunctionMaxQuery((DisjunctionMaxQuery) in, searcher);
        if (in instanceof TermInSetQuery)
            return rewriteTermInSetQuery((TermInSetQuery) in);
        if (in instanceof BoostQuery)
            return rewrite(((BoostQuery) in).getQuery(), searcher);   // we don't care about boosts for rewriting purposes
        if (in instanceof PhraseQuery)
            return rewritePhraseQuery((PhraseQuery)in);
        if (in instanceof ConstantScoreQuery)
            return rewrite(((ConstantScoreQuery) in).getQuery(), searcher);
        if (searcher != null) {
            return rewrite(searcher.rewrite(in), null);
        }

        return rewriteUnknown(in);
    }
	
	@Override
	protected Query rewriteBoolean(BooleanQuery bq, IndexSearcher searcher) throws RewriteException, IOException {
        BooleanQuery.Builder newbq = new BooleanQuery.Builder();
        newbq.setMinimumNumberShouldMatch(bq.getMinimumNumberShouldMatch());
        for (BooleanClause clause : bq) {
        	Query q = clause.getQuery();
        	if (q instanceof MatchAllDocsQuery) {
        		continue;
        	}
        	
            BooleanClause.Occur occur = clause.getOccur();
            if (occur == BooleanClause.Occur.FILTER)
                occur = BooleanClause.Occur.MUST;   // rewrite FILTER to MUST to ensure scoring
            newbq.add(rewrite(clause.getQuery(), searcher), occur);
        }
        return newbq.build();
    }

	private Query rewriteSynonymQuery(SynonymQuery in, IndexSearcher searcher) throws RewriteException {
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		
		try {
			for (Term term : in.getTerms()) {
				Query q = rewrite(new TermQuery(term), searcher);
				builder.add(new BooleanClause(q, Occur.SHOULD));
			}

		} catch (RewriteException | IOException e) {
			throw new RewriteException("Error rewriting query: " + e.getMessage(), in);
		}
		
		return builder.build();
	}
}
