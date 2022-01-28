package org.elasticsearch.mysynonym;


import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.Version;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.search.MatchQuery;

import java.io.IOException;
import java.util.Objects;

/**
 * Match query is a query that analyzes the text and constructs a query as the
 * result of the analysis.
 */
public class SynonymMatchBuilder extends MatchQueryBuilder {

    public static final String NAME = "match_enhance";

    public static final ParseField SYNONYM_BOOST_FIELD = new ParseField("synonym_boost");

    // 同义词贡献 得分 为 0 是理想的情况
    private float synonymBoost = SynonymMatchQuery.DEFAULT_SYNONYM_BOOST;


    /**
     * Constructs a new match query.
     */
    public SynonymMatchBuilder(String fieldName, Object value) {
        super(fieldName, value);
    }

    /**
     * Read from a stream.
     */
    public SynonymMatchBuilder(StreamInput in) throws IOException {
        super(in);

        // optional fields
        synonymBoost = in.readFloat();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName());
        out.writeGenericValue(value());
        if (out.getVersion().before(Version.V_6_0_0_rc1)) {
            MatchQuery.Type.BOOLEAN.writeTo(out); // deprecated type
        }
        operator().writeTo(out);
        if (out.getVersion().before(Version.V_6_0_0_rc1)) {
            out.writeVInt(MatchQuery.DEFAULT_PHRASE_SLOP); // deprecated slop
        }
        out.writeVInt(prefixLength());
        out.writeVInt(maxExpansions());
        out.writeBoolean(fuzzyTranspositions());
        out.writeBoolean(lenient());
        zeroTermsQuery().writeTo(out);
        // optional fields
        out.writeOptionalString(analyzer());
        out.writeOptionalString(minimumShouldMatch());
        out.writeOptionalString(fuzzyRewrite());
        out.writeOptionalWriteable(fuzziness());
        out.writeOptionalFloat(cutoffFrequency());
        if (out.getVersion().onOrAfter(Version.V_6_1_0)) {
            out.writeBoolean(autoGenerateSynonymsPhraseQuery());
        }
        System.out.println("synonymBoost:" + synonymBoost);
        out.writeFloat(synonymBoost);
    }

    public float synonymBoost() {
        return synonymBoost;
    }

    public SynonymMatchBuilder synonymBoost(float synonymBoost) {
        this.synonymBoost = synonymBoost;
        return this;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        System.out.println(params);
        builder.startObject(NAME);
        builder.startObject(this.fieldName());
        builder.field(QUERY_FIELD.getPreferredName(), this.value());
        builder.field(OPERATOR_FIELD.getPreferredName(), this.operator().toString());


        if (this.fuzziness() != null) {
            this.fuzziness().toXContent(builder, params);
        }

        builder.field(PREFIX_LENGTH_FIELD.getPreferredName(), this.prefixLength());
        builder.field(MAX_EXPANSIONS_FIELD.getPreferredName(), this.maxExpansions());
        if (this.minimumShouldMatch() != null) {
            builder.field(MINIMUM_SHOULD_MATCH_FIELD.getPreferredName(), this.minimumShouldMatch());
        }

        if (this.fuzzyRewrite() != null) {
            builder.field(FUZZY_REWRITE_FIELD.getPreferredName(), this.fuzzyRewrite());
        }
        builder.field(SYNONYM_BOOST_FIELD.getPreferredName(), synonymBoost);
        builder.field(FUZZY_TRANSPOSITIONS_FIELD.getPreferredName(), this.fuzzyTranspositions());
        builder.field(LENIENT_FIELD.getPreferredName(), this.lenient());
        builder.field(ZERO_TERMS_QUERY_FIELD.getPreferredName(), this.zeroTermsQuery().toString());
        if (this.cutoffFrequency() != null) {
            builder.field(CUTOFF_FREQUENCY_FIELD.getPreferredName(), this.cutoffFrequency());
        }

        builder.field(GENERATE_SYNONYMS_PHRASE_QUERY.getPreferredName(), this.autoGenerateSynonymsPhraseQuery());
        this.printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
        System.out.println(builder.prettyPrint());
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        System.out.println(context);
        // validate context specific fields
        if (analyzer() != null && context.getIndexAnalyzers().get(analyzer()) == null) {
            throw new QueryShardException(context, "[" + NAME + "] analyzer [" + analyzer() + "] not found");
        }

        SynonymMatchQuery matchQuery = new SynonymMatchQuery(context);
        if (analyzer() != null) {
            matchQuery.setAnalyzer(analyzer());
        }
        matchQuery.setSynonymBoost(synonymBoost);
        return matchQuery.parse(fieldName(), value());
//        return Queries.maybeApplyMinimumShouldMatch(query, minimumShouldMatch);
    }

    @Override
    protected boolean doEquals(MatchQueryBuilder other) {
        SynonymMatchBuilder otherBuilder = (SynonymMatchBuilder) other;

        return Objects.equals(fieldName(), otherBuilder.fieldName()) &&
                Objects.equals(value(), otherBuilder.value()) &&
                Objects.equals(operator(), otherBuilder.operator()) &&
                Objects.equals(analyzer(), otherBuilder.analyzer()) &&
                Objects.equals(fuzziness(), otherBuilder.fuzziness()) &&
                Objects.equals(prefixLength(), otherBuilder.prefixLength()) &&
                Objects.equals(maxExpansions(), otherBuilder.maxExpansions()) &&
                Objects.equals(minimumShouldMatch(), otherBuilder.minimumShouldMatch()) &&
                Objects.equals(fuzzyRewrite(), otherBuilder.fuzzyRewrite()) &&
                Objects.equals(lenient(), otherBuilder.lenient()) &&
                Objects.equals(fuzzyTranspositions(), otherBuilder.fuzzyTranspositions()) &&
                Objects.equals(zeroTermsQuery(), otherBuilder.zeroTermsQuery()) &&
                Objects.equals(cutoffFrequency(), otherBuilder.cutoffFrequency()) &&
                Objects.equals(this.synonymBoost, otherBuilder.synonymBoost) &&
                Objects.equals(autoGenerateSynonymsPhraseQuery(), otherBuilder.autoGenerateSynonymsPhraseQuery());
    }

    @Override
    protected int doHashCode() {
        return Objects.hash(fieldName(), value(), operator(), analyzer(),
                fuzziness(), prefixLength(), maxExpansions(), minimumShouldMatch(),
                fuzzyRewrite(), lenient(), fuzzyTranspositions(), zeroTermsQuery(), cutoffFrequency(),synonymBoost , autoGenerateSynonymsPhraseQuery());
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    public static SynonymMatchBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        Object value = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String minimumShouldMatch = null;
        String analyzer = null;
        Operator operator = MatchQueryBuilder.DEFAULT_OPERATOR;
        Fuzziness fuzziness = null;
        int prefixLength = FuzzyQuery.defaultPrefixLength;
        int maxExpansion = FuzzyQuery.defaultMaxExpansions;
        boolean fuzzyTranspositions = FuzzyQuery.defaultTranspositions;
        String fuzzyRewrite = null;
        boolean lenient = MatchQuery.DEFAULT_LENIENCY;
        Float cutOffFrequency = null;
        MatchQuery.ZeroTermsQuery zeroTermsQuery = MatchQuery.DEFAULT_ZERO_TERMS_QUERY;
        boolean autoGenerateSynonymsPhraseQuery = true;
        String queryName = null;
        String currentFieldName = null;
        float synonyBoost = DEFAULT_BOOST;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else if (token.isValue()) {
                        if (QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            value = parser.objectText();
                        } else if (ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            analyzer = parser.text();
                        } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            boost = parser.floatValue();
                        } else if (Fuzziness.FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzziness = Fuzziness.parse(parser);
                        } else if (PREFIX_LENGTH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            prefixLength = parser.intValue();
                        } else if (MAX_EXPANSIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            maxExpansion = parser.intValue();
                        } else if (OPERATOR_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            operator = Operator.fromString(parser.text());
                        } else if (MINIMUM_SHOULD_MATCH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            minimumShouldMatch = parser.textOrNull();
                        } else if (FUZZY_REWRITE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzzyRewrite = parser.textOrNull();
                        } else if (FUZZY_TRANSPOSITIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            fuzzyTranspositions = parser.booleanValue();
                        } else if (LENIENT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            lenient = parser.booleanValue();
                        } else if (CUTOFF_FREQUENCY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            cutOffFrequency = parser.floatValue();
                        } else if (ZERO_TERMS_QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            String zeroTermsValue = parser.text();
                            if ("none".equalsIgnoreCase(zeroTermsValue)) {
                                zeroTermsQuery = MatchQuery.ZeroTermsQuery.NONE;
                            } else if ("all".equalsIgnoreCase(zeroTermsValue)) {
                                zeroTermsQuery = MatchQuery.ZeroTermsQuery.ALL;
                            } else {
                                throw new ParsingException(parser.getTokenLocation(),
                                        "Unsupported zero_terms_query value [" + zeroTermsValue + "]");
                            }
                        } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            queryName = parser.text();
                        } else if (GENERATE_SYNONYMS_PHRASE_QUERY.match(currentFieldName, parser.getDeprecationHandler())) {
                            autoGenerateSynonymsPhraseQuery = parser.booleanValue();
                        }else if (SYNONYM_BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())){
                            synonyBoost = parser.floatValue();
                        } else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    "[" + NAME + "] query does not support [" + currentFieldName + "]");
                        }
                    } else {
                        throw new ParsingException(parser.getTokenLocation(),
                                "[" + NAME + "] unknown token [" + token + "] after [" + currentFieldName + "]");
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = parser.currentName();
                value = parser.objectText();
            }
        }

        if (value == null) {
            throw new ParsingException(parser.getTokenLocation(), "No text specified for text query");
        }

        SynonymMatchBuilder matchQuery = new SynonymMatchBuilder(fieldName, value);
        matchQuery.operator(operator);
        matchQuery.analyzer(analyzer);
        matchQuery.minimumShouldMatch(minimumShouldMatch);
        if (fuzziness != null) {
            matchQuery.fuzziness(fuzziness);
        }
        matchQuery.fuzzyRewrite(fuzzyRewrite);
        matchQuery.prefixLength(prefixLength);
        matchQuery.fuzzyTranspositions(fuzzyTranspositions);
        matchQuery.maxExpansions(maxExpansion);
        matchQuery.lenient(lenient);
        if (cutOffFrequency != null) {
            matchQuery.cutoffFrequency(cutOffFrequency);
        }
        matchQuery.zeroTermsQuery(zeroTermsQuery);
        matchQuery.synonymBoost(synonyBoost);
        matchQuery.autoGenerateSynonymsPhraseQuery(autoGenerateSynonymsPhraseQuery);
        matchQuery.queryName(queryName);
        matchQuery.boost(boost);
        return matchQuery;
    }

}
