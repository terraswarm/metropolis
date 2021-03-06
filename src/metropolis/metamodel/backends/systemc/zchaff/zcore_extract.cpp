/*********************************************************************
 Copyright 2000-2003, Princeton University.  All rights reserved.
 By using this software the USER indicates that he or she has read,
 understood and will comply with the following:

 --- Princeton University hereby grants USER nonexclusive permission
 to use, copy and/or modify this software for internal, noncommercial,
 research purposes only. Any distribution, including commercial sale
 or license, of this software, copies of the software, its associated
 documentation and/or modifications of either is strictly prohibited
 without the prior consent of Princeton University.  Title to copyright
 to this software and its associated documentation shall at all times
 remain with Princeton University.  Appropriate copyright notice shall
 be placed on all software copies, and a complete copy of this notice
 shall be included in all copies of the associated documentation.
 No right is  granted to use in advertising, publicity or otherwise
 any trademark, service mark, or the name of Princeton University.


 --- This software and any associated documentation is provided "as is"

 PRINCETON UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS
 OR IMPLIED, INCLUDING THOSE OF MERCHANTABILITY OR FITNESS FOR A
 PARTICULAR PURPOSE, OR THAT  USE OF THE SOFTWARE, MODIFICATIONS, OR
 ASSOCIATED DOCUMENTATION WILL NOT INFRINGE ANY PATENTS, COPYRIGHTS,
 TRADEMARKS OR OTHER INTELLECTUAL PROPERTY RIGHTS OF A THIRD PARTY.

 Princeton University shall not be liable under any circumstances for
 any direct, indirect, special, incidental, or consequential damages
 with respect to any claim by USER or any third party on account of
 or arising from the use, or inability to use, this software or its
 associated documentation, even if Princeton University has been advised
 of the possibility of those damages.
*********************************************************************/

#include <sys/time.h>
#include <sys/resource.h>
#include <unistd.h>
#include <cstdio>
#include <vector>
#include <set>
#include <algorithm>
#include <iostream>
#include <fstream>

#include "mystl_hash.h"

using namespace std;

const int MAX_BUFF_SIZE = 1024* 1024 * 4;
const int CLS_COUNT_ARRAY_SIZE = 1024 * 1024;
const int WORD_LEN = 65536;

#define MEM_LIMIT 800000

#define UNKNOWN 2

int _peak_mem;
char * _core_file_name = "unsat_core.cnf";
//===================================================================================
double get_cpu_time()
{
    double res;
    struct rusage usage;

    getrusage(RUSAGE_SELF, &usage);

    res = usage.ru_utime.tv_usec + usage.ru_stime.tv_usec;
    res *= 1e-6;
    res += usage.ru_utime.tv_sec + usage.ru_stime.tv_sec;

    return res;
}

void get_line(ifstream &fs, vector<char> &buf)
{
    buf.clear();
    buf.reserve(4096);
    while (!fs.eof()) {
        char ch = fs.get();
        if (ch == '\n' || ch == '\377')
            break;
        if (ch == '\r')
            continue;
        buf.push_back(ch);
    }
    buf.push_back('\0');
    return;
}

int get_token (char * & lp, char * token)
{
    char * wp = token;
    while (*lp && ((*lp == ' ') || (*lp == '\t'))) {
        lp++;
    }
    while (*lp && (*lp != ' ') && (*lp != '\t') && (*lp != '\n')) {
        *(wp++) = *(lp++);
    }
    *wp = '\0';                                 // terminate string
    return wp - token;
}

int get_mem_usage(void)
{
    //        return 0;        //for operating systems other than Linux

    FILE * fp;
    char buffer[128];
    char token[128];
    char filename[128];

    int pid = getpid();
    sprintf(filename, "/proc/%i/status", pid);
    if ( (fp = fopen (filename, "r")) == NULL) {
        cerr << "Can't open Proc file, are you sure you are using Linux?" << endl;
        cerr << "If you are not using Linux, please uncomment the first line in get_mem_usage()" << endl;
        exit(1);
    }
    while (!feof(fp)) {
        fgets(buffer, 128, fp);
        char * ptr = buffer;
        get_token(ptr, token);
        if (strcmp(token, "VmSize:")==0) {
            get_token(ptr, token);
            fclose(fp);
            return atoi(token);
        }
    }
    cerr << "Error in getting memeory usage." << endl;
    exit(1);
    return 0;
}

int my_a2i(char * str)
{
    int result = 0;
    bool neg = false;
    if (str[0] == '-') {
        neg = true;
        ++ str;
    }
    else if (str[0] == '+')
        ++ str;
    for (unsigned i=0, sz = strlen(str); i< sz; ++i) {
        int d = str[i] - '0';
        if (d < 0 || d > 9) {
            cerr << "Abort: Unable to change " << str << " into an integer." << endl;
            exit(1);
        }
        result = result * 10 + d;
    }
    if (neg)
        result = -result;
    return result;
}
//=====================================================================================
class CClause
{
public:
    vector<int> literals;
};

class CVariable
{
public:
    short value;

    short in_clause_phase        :4;

    bool is_needed                :1;

    int antecedent;

    int num_lits[2];

    int level;

    vector<int> claimed_ante_lits;

    CVariable(void) {
        value = UNKNOWN;
        antecedent = -1;
        in_clause_phase = UNKNOWN;
        num_lits[0] = num_lits[1] = 0;
        level = -1;
        is_needed = false;
    }
};

struct cmp_var_level
{
    bool operator () (CVariable * v1, CVariable * v2)
    {
        if (v1->level > v2->level)
            return true;
        else if (v1->level < v2->level)
            return false;
        else if ( (int)v1 > (int)v2)
            return true;
        return false;
    }
};

class CDatabase {
private:
    int _current_num_clauses;

    FILE * _tmp_rsource_fp;

    int        _num_init_clauses;

    vector<CVariable> _variables;

    vector<CClause> _clauses;

    int _conf_id;

    vector<int> _conf_clause;

    vector<int> _empty_r_source;
public:
    CDatabase() {
        _tmp_rsource_fp = tmpfile();
        if (_tmp_rsource_fp == NULL) {
            cerr << "Can't Open Temp File " << endl;
            exit(1);
        }
        _num_init_clauses = 0;
        _current_num_clauses = 0;
        _conf_id = -1;
    }

    int & num_init_clauses(void) { return _num_init_clauses; }

    vector<CVariable> & variables(void) {return _variables; }

    vector<CClause> & clauses(void)  {return  _clauses; }

    void read_cnf(char * filename);

    void parse_trace(char * filename);

    void produce_core(char * filename);

    int lit_value(int svar) {
        assert (_variables[svar>>1].value != UNKNOWN);
        return _variables[svar>>1].value ^ (svar&0x1);
    }

    int add_orig_clause_by_lits(vector<int> lits);

    void set_var_number(int nvar);

    void set_init_cls_number (int n) {
        _num_init_clauses = n;
    }
    void construct_learned_clauses(void);

    FILE * reverse_file(FILE * fp_in);

    void print_file(FILE * fp);

    void calculate_unsat_core(void);

    void dump(void);
};
//================================================================================

void CDatabase::dump(void)
{
    cout << "p cnf " << _variables.size() - 1 << " " << _num_init_clauses << endl;
    for (unsigned i=0; i< _clauses.size(); ++i) {
        for (unsigned j=0; j< _clauses[i].literals.size(); ++j ) {
            int lit = _clauses[i].literals[j];
            cout << ((lit & 0x1)?"-":"") << (lit>>1) << " ";
        }
        cout << "0" << endl;
    }
}

void CDatabase::set_var_number(int nvar)
{
    _variables.resize(nvar + 1);
    for (unsigned i=0; i < _variables.size(); ++i) {
        _variables[i].value = UNKNOWN;
        _variables[i].in_clause_phase = UNKNOWN;
    }
}

void check_mem_out(void)
{
    int mem = get_mem_usage();
    if (mem > MEM_LIMIT) {
        cerr << "Mem out" << endl;
        exit(1);
    }
    if (mem > _peak_mem)
        _peak_mem = mem;
}

int CDatabase::add_orig_clause_by_lits(vector<int> lits)
{
    static int line_n = 0;
    ++ line_n;
    if (lits.size() == 0) {
        cerr << "Empty Clause Encountered " << endl;
        exit(1);
    }
    int cls_id = _clauses.size();
    _clauses.resize(_clauses.size() + 1);
    vector<int> temp_cls;
    for (unsigned i=0; i< lits.size(); ++i) {
        int vid = lits[i];
        int phase = 0;
        if (vid < 0) {
            vid = - vid;
            phase = 1;
        }
        if (vid == 0 || vid >(int)_variables.size() - 1) {
            cerr << "Variable index out of range " << endl;
            exit(1);
        }
        if (_variables[vid].in_clause_phase == UNKNOWN) {
            _variables[vid].in_clause_phase = phase;
            temp_cls.push_back(vid + vid + phase);
            ++ _variables[vid].num_lits[phase];
        }
        else if (_variables[vid].in_clause_phase != phase) {
            cerr << "clause " << line_n << "  :";
            for (unsigned j=0; j< lits.size(); ++j )
                cerr << lits[j] ;
            cerr << endl << "A clause contain a literal and its negate " << endl;
            exit(1);
        }
    }
    _clauses[cls_id].literals.resize(temp_cls.size());
    for (unsigned i=0; i< temp_cls.size(); ++i) {
        _clauses[cls_id].literals[i]= temp_cls[i];
    }
    for (unsigned i=0; i< lits.size(); ++i) {
        int vid = lits[i];
        if (vid < 0) vid = -vid;
        _variables[vid].in_clause_phase = UNKNOWN;
    }
    ++ _current_num_clauses;
    if (_current_num_clauses%10 == 0)
        check_mem_out();
    return cls_id;
}


void CDatabase::read_cnf (char * filename)
{
    ifstream in_file (filename);
    if (!in_file) {
        cerr << "Can't open input CNF file " << filename << endl;
        exit(1);
    }

    vector<char> buffer;
    vector<int> literals;
    bool header_encountered = false;
    char token[WORD_LEN];
    while (!in_file.eof()) {
        get_line(in_file, buffer);
        char * ptr = &(*buffer.begin());
        if (get_token(ptr, token)) {
            if (strcmp(token, "c")==0)
                continue;
            else if (strcmp(token, "p")==0) {
                get_token(ptr, token);
                if (strcmp(token, "cnf") != 0) {
                    cerr << "Format Error, p cnf NumVar NumCls " << endl;
                    exit(1);
                }
                get_token(ptr, token);
                int nvar = my_a2i(token);
                set_var_number(nvar);
                get_token(ptr, token);
                int ncls = my_a2i(token);
                set_init_cls_number(ncls);
                header_encountered = true;
                continue;
            }
            else {
                int lit = my_a2i(token);
                if (lit != 0)
                    literals.push_back(lit);
                else {
                    add_orig_clause_by_lits(literals);
                    literals.clear();
                }
            }
        }
        while (get_token(ptr, token)) {
            int lit = my_a2i(token);
            if (lit != 0)
                literals.push_back(lit);
            else {
                add_orig_clause_by_lits(literals);
                literals.clear();
            }
        }
    }
    if (!literals.empty()) {
        cerr << "Trailing literals without termination in the last clause" << endl;
        exit(1);
    }
    if (clauses().size() != (unsigned) num_init_clauses()) {
        cerr << "WARNING : Clause count inconsistant with the header " << endl;
        cerr << "Header indicates " << num_init_clauses() << " Clauses " << endl;
    }
    cout << "Successfully read " << num_init_clauses() << " Clauses " << endl;
}

void CDatabase::produce_core(char * filename)
{
    parse_trace(filename);
    set<CVariable *, cmp_var_level> clause_lits;
    for (unsigned i=0; i< _conf_clause.size();  ++i) {
        int vid = (_conf_clause[i]>> 1);
        clause_lits.insert(&_variables[vid]);
    }
    assert (clause_lits.size() == _conf_clause.size());
    _empty_r_source.push_back(_conf_id);
    while (!clause_lits.empty()) {
        int vid = (*clause_lits.begin() - &_variables[0]);
        int ante = _variables[vid].antecedent;
        if (ante == -1) {
            cerr << "Variable " << vid << " has an NULL antecedent ";
            exit(1);
        }
        clause_lits.erase(clause_lits.begin());
        _empty_r_source.push_back(ante);
        vector<int> & lits = _variables[vid].claimed_ante_lits;
        for (unsigned i=0; i< lits.size(); ++i) {
            int l = lits[i];
            int v = (l>>1);
            if (v != vid)
                clause_lits.insert(&_variables[v]);
        }
    }
    cout << "Empty clause resolve-sources generated. " << endl;
    cout << "Mem Usage:\t\t\t\t" << get_mem_usage()<< endl;
    calculate_unsat_core();
}

void CDatabase::parse_trace(char * filename)
{
    vector<char> buffer;
    char token [WORD_LEN];

    ifstream in_file (filename);
    if (!in_file) {
        cerr << "Can't open input CNF file " << filename << endl;
        exit(1);
    }

    while (!in_file.eof()) {
        get_line(in_file, buffer);
        char * ptr = &(*buffer.begin());
        get_token(ptr, token);
        if (strcmp (token, "CL:") == 0) {
            vector<int> resolvents;

            get_token(ptr, token);
            int cl_id = my_a2i(token);

            get_token(ptr, token);
            assert (strcmp(token, "<=") == 0);

            while (get_token(ptr, token)) {
                int r = my_a2i(token);
                resolvents.push_back(r);
            }
            int storage[resolvents.size() + 2];
            storage[0] = - cl_id;
            storage[1] = - resolvents.size();
            for (unsigned j=0; j< resolvents.size(); ++j)
                storage[j+2] = resolvents[j];
            fwrite(storage, sizeof(int), resolvents.size() + 2, _tmp_rsource_fp);
        }
        else if (strcmp (token, "VAR:") == 0) {
            get_token(ptr,token);
            int vid = my_a2i(token);

            get_token(ptr,token);
            assert (strcmp(token, "L:") == 0);
            get_token(ptr, token);
            int lev = my_a2i(token);

            get_token(ptr,token);
            assert (strcmp(token, "V:") == 0);
            get_token(ptr,token);
            int value = my_a2i(token);
            assert (value == 1 || value == 0);

            get_token(ptr,token);
            assert (strcmp(token, "A:") == 0);
            get_token(ptr,token);
            int ante = my_a2i(token);

            get_token(ptr,token);
            assert (strcmp(token, "Lits:") == 0);
            vector<int> cl_literals;
            while (get_token(ptr, token)) {
                int r = my_a2i(token);
                cl_literals.push_back(r);
            }

            _variables[vid].value = value;
            _variables[vid].antecedent = ante;
            _variables[vid].level = lev;
            for (unsigned j=0; j< cl_literals.size(); ++j)
                _variables[vid].claimed_ante_lits.push_back(cl_literals[j]);
        }
        else if (strcmp (token, "CONF:") == 0) {
            get_token(ptr,token);
            _conf_id = my_a2i(token);

            get_token(ptr,token);
            assert (strcmp(token, "==") == 0);

            while (get_token(ptr, token)) {
                int lit = my_a2i(token);
                assert (lit > 0);
                assert ( (unsigned)(lit>>1) < _variables.size());
                _conf_clause.push_back(lit);
            }
        }
    }
    if (_conf_id == -1) {
        cerr << "No final conflicting clause defined " << endl;
        exit (1);
    }
    cout << "Mem Usage After Read in File:\t\t" << get_mem_usage() << endl;
}

FILE * CDatabase::reverse_file(FILE * fp_in)
{
    //currently fp_in point to the end of the input file
    assert ((unsigned)MAX_BUFF_SIZE > _variables.size() * 2
            && "Buffer must be able to contain at least 2 biggest clauses");
    int * read_buff;
    read_buff = (int *) malloc((MAX_BUFF_SIZE + _variables.size())*sizeof(int));
    int * write_buff;
    write_buff = (int*) malloc((MAX_BUFF_SIZE + _variables.size()) * sizeof(int));

    long file_size = ftell(fp_in)/sizeof(int);
    assert (ftell(fp_in)%sizeof(int) == 0);
    int num_trunks = file_size/MAX_BUFF_SIZE;

    FILE * fp = tmpfile();
    int last_remain = 0;
    for (int i=0; i< num_trunks; ++i) {
        cout << i << " ";
        cout.flush();
        fseek(fp_in, -MAX_BUFF_SIZE*sizeof(int), SEEK_CUR);
        int r = fread(read_buff, sizeof(int), MAX_BUFF_SIZE, fp_in);
        assert (r == MAX_BUFF_SIZE);
        fseek(fp_in, -MAX_BUFF_SIZE*sizeof(int), SEEK_CUR);
        int write_idx = 0;
        for (int index=MAX_BUFF_SIZE - 1 + last_remain; index>= 1; --index) {
            if (read_buff[index] < 0) {
                assert (read_buff[index -1] < 0);
                int num_lits = -read_buff[index];
                -- index;
                for (int j=0; j< num_lits + 2; ++j)
                    write_buff[write_idx ++] = read_buff[j + index];
            }
        }
        fwrite(write_buff, sizeof(int), write_idx, fp);
        last_remain = 0;
        for (int j = 0; j < MAX_BUFF_SIZE; ++j) {
            if (read_buff[j] < 0 && read_buff[j+1] < 0)
                break;
            else
                read_buff[j + MAX_BUFF_SIZE] = read_buff[j];
            ++ last_remain;
        }
    }
    //the last trunk
    int last_trunk_size = file_size%MAX_BUFF_SIZE;
    int last_trunk_begin = MAX_BUFF_SIZE - last_trunk_size;
    assert (ftell(fp_in) == (long) (last_trunk_size * sizeof(int)));
    fseek(fp_in, -last_trunk_size*sizeof(int), SEEK_CUR);
    fread(read_buff + last_trunk_begin, sizeof(int), last_trunk_size, fp_in);
    int index;
    int write_idx = 0;

    for (index = MAX_BUFF_SIZE-1 + last_remain; index >= last_trunk_begin + 1; --index) {
        if (read_buff[index] < 0) {
            assert (read_buff[index -1] < 0);
            int num_lits = -read_buff[index];
            -- index;
            for (int j=0; j< num_lits + 2; ++j)
                write_buff[write_idx ++] = read_buff[j + index];
        }
    }
    fwrite(write_buff, sizeof(int), write_idx, fp);
    assert (read_buff[last_trunk_begin] < 0 &&
            read_buff[last_trunk_begin+1] < 0);
    check_mem_out();
    free(read_buff);
    free(write_buff);
    return fp;
}

void CDatabase::print_file(FILE * fp)
{
    ofstream out("out_file");
    int inputs[_variables.size()];
    int info[2];
    int pos = ftell(fp);
    rewind(fp);
    fread(info, sizeof(int), 2, fp);
    while (!feof(fp)) {
        assert (info[0] < 0);
        assert (info[1] < 0);
        unsigned num_in = -info[1];
        assert ( num_in < _variables.size());
        fread(inputs, sizeof(int), num_in, fp);
        out << "CL : " << -info[0] << " Num: "<< num_in << " :";
        for (unsigned i=0; i< num_in; ++i)
            out << inputs[i] << " ";
        out << endl;
        fread(info, sizeof(int), 2, fp);
    }
    fseek (fp, pos, SEEK_SET);
}

void CDatabase::calculate_unsat_core(void)
{
    int num_edges = 0;
    int num_nodes = 0;
    int max_involved = 0;
    FILE * r_source_fp;
    r_source_fp = reverse_file(_tmp_rsource_fp);
    fclose(_tmp_rsource_fp);
    rewind(r_source_fp);
    hash_set<int> involved;
    for (unsigned i=0; i< _empty_r_source.size(); ++i)
        involved.insert(_empty_r_source[i]);
    max_involved =involved.size();

    int r_source[_variables.size()];
    int info[2];

    fread(info, sizeof(int), 2, r_source_fp);
    while (!feof(r_source_fp)) {
        assert (info[0] < 0);
        assert (info[1] < 0);
        int cl_id = -info[0];
        unsigned  num_srcs = -info[1];
        num_edges += num_srcs;
        num_nodes ++;
        assert ( num_srcs < _variables.size());
        fread(r_source, sizeof(int), num_srcs, r_source_fp);
        if (involved.find(cl_id) != involved.end()) {
            involved.erase(cl_id);
            for (unsigned i=0; i< num_srcs; ++i) {
                int s= r_source[i];
                assert (s < cl_id);
                involved.insert(s);
            }
            if (max_involved < (int)involved.size())
                max_involved = involved.size();
        }
        fread(info, sizeof(int), 2, r_source_fp);
    }
    fclose(r_source_fp);

    int needed_cls_count = 0;
    int needed_var_count = 0;
    for (int i=0; i< num_init_clauses(); ++i) {
        if (involved.find(i) != involved.end()) {
            ++ needed_cls_count;
            CClause & cl = _clauses[i];
            for (unsigned j=0; j< cl.literals.size(); ++j) {
                int vid = (cl.literals[j] >> 1);
                if (_variables[vid].is_needed == false) {
                    ++ needed_var_count;
                    _variables[vid].is_needed = true;
                }
            }
        }
    }
    cout << "Num. Learned Clauses\t\t\t" << num_nodes << endl;
    cout << "Num. Resolve Sources\t\t\t" << num_edges << endl;
    cout << "Max Hash Size\t\t\t\t" << max_involved << endl;
    cout << "Original Num. Clauses:\t\t\t" << num_init_clauses() << endl;
    cout << "Needed Clauses to Construct Empty:\t"<< needed_cls_count << endl;
    cout << "Total Variable count:\t\t\t" << _variables.size()-1 << endl;
    cout << "Variables involved in Empty:\t\t" << needed_var_count << endl;

    ofstream dump(_core_file_name);

    dump << "c Variables Not Involved: ";
    unsigned int k=0;
    for (unsigned i=1; i< _variables.size(); ++i) {
        if (_variables[i].is_needed == false) {
            if (k%20 == 0)
                dump << endl << "c ";
            ++k;
            dump << i << " ";
        }
    }
    dump << endl;
    dump << "p cnf " << _variables.size()-1 << " " << needed_cls_count << endl;
    for (int i=0; i< num_init_clauses(); ++i) {
        if (involved.find(i) != involved.end()) {
            CClause & cl = _clauses[i];
            dump << "c Original Cls ID: " << i << endl;
            for (unsigned j=0; j< cl.literals.size(); ++j)
                dump << ((cl.literals[j] & 0x1)?" -":" ") << (cl.literals[j] >> 1);
            dump << " 0" << endl;
        }
    }
}

int main(int argc, char * * argv)
{
    cout << "ZExtract: UnSAT Core Extractor" << endl;
    cout << "Copyright Princeton University, 2003. All Right Reserved." << endl;
    if (argc != 3 && argc != 4) {
        cerr << "Usage: " << argv[0]
             << " CNF_File  Resolve_Trace  [Core_Filename = unsat_core.cnf]" << endl;
        cerr << endl;
        exit(1);
    }
    cout << "COMMAND LINE: ";
    for (int i=0; i< argc; ++i)
        cout << argv[i] << " ";
    cout << endl;
    if (argc == 4)
        _core_file_name = argv[3];

    _peak_mem = get_mem_usage();

    CDatabase dbase;

    double begin_time = get_cpu_time();
    cout << "Read in original clauses ... " << endl;
    dbase.read_cnf(argv[1]);
    dbase.produce_core(argv[2]);
    double end_time = get_cpu_time();
    cout << "Unsat Core Produced Successfully:\t" << _core_file_name << endl;
    cout << "CPU Time:\t\t\t\t" << end_time - begin_time << endl;
    cout << "Peak Mem Usage:\t\t\t\t" << _peak_mem << endl;
}
