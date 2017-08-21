import beepp.util.RangeUnion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SuppressWarnings({"CodeBlock2Expr", "unused", "UnnecessaryLocalVariable"})
class FormulaBuilder {
    private final List<Tree> trees;
    private final boolean hasFictitiousRoot;
    private final int n;  // Taxa = {1...n} + fictitious {0}
    private final int k;  // Network hybridisation number
    private final int m1; // Maximum common-vertex children
    private final int m2; // Maximum reticular-vertex parents
    private final List<String> clauses = new ArrayList<>();

    FormulaBuilder(List<Tree> trees,
                   int hybridisationNumber,
                   int maximumChildren,
                   int maximumParents) {
        this.trees = trees;
        this.hasFictitiousRoot = trees.get(0).hasFictitiousRoot();
        this.n = trees.get(0).getTaxaSize();
        this.k = hybridisationNumber;
        this.m1 = maximumChildren;
        this.m2 = maximumParents;

        // System.out.clause("n = " + n + (hasFictitiousRoot ? "+1" : "") + ", k = " + k);
        // System.out.clause("L = " + makeList(L()));
        // System.out.clause("V = " + makeList(V()));
        // System.out.clause("R = " + makeList(R()));
        // System.out.clause("T = " + makeList(T()));
    }

    Formula build() {
        declareVariables();     // [5/5] TODO: check
        declareConstraints();   // [2/2] TODO: check
        return new Formula(clauses);
    }


    private void declareVariables() {
        clause("// 1. Variables declaration");

        /* Network structure */
        clause("// 1.1 Network structure variables");
        LV_().forEach(u -> {
            declareInt(var("p", u), PP(u));
        });

        R().forEach(r -> {
            PP(r).forEach(i -> {
                declareBool(var("p", r, i));
            });
        });

        /* Trees to network mapping */
        clause("// 1.2 Trees to network mapping variables");
        T().forEach(t -> {
            VR().forEach(v -> {
                declareBool(var("u", t, v));
            });
        });

        T().forEach(t -> {
            // L().forEach(v -> {
            //     declareInt(var("x", t, v), IntStream.of(v));
            // });
            VR().forEach(v -> {
                declareInt(var("x", t, v), LVt());
            });
        });

        T().forEach(t -> {
            R().forEach(r -> {
                declareInt(var("pt", t, r), PP(r));
            });
        });
    }


    private void declareInt(String name, IntStream domain) {
        RangeUnion ranges = new RangeUnion();
        // TODO improve performance
        domain.forEach(x -> ranges.addRange(x, x));
        if (ranges.isEmpty())
            throw new IllegalArgumentException("Trying to declare int with an empty domain (name: " + name + ")");
        declareInt(name, ranges);
    }

    private void declareInt(String name, RangeUnion domain) {
        clause("dual_int %s: %s", name, domain.toBEEppString());
    }

    private void declareBool(String name) {
        clause("bool %s", name);
    }


    private void declareConstraints() {
        clause("// 2. Constraints declaration");
        declareNetworkStructureConstraints();       // [4/4] TODO: check
        declareTreesToNetworkMappingConstraints();  // [4/4] TODO: check
    }


    private void declareNetworkStructureConstraints() {
        clause("// 2.1 Network structure constraints");
        boolean vertices_alo_amo = true;
        boolean reticular_one = true;
        boolean retucular_alo_amo = true;
        // boolean leaves_ = true;
        boolean numeration_reticular_through = true;  // need
        boolean numeration_vertices_through = true;  // seems like we need it
        boolean numeration_reticular_order = true;  // need
        boolean numeration_vertices_order = false;  // excess

        clause("// 2.1.1 Vertices (forall v in V)");
        // clause("// ONE(p_{v,i})_i");
        // no need

        clause("// AL2(p_{i,v})_i  and  AMM1(p_{i,v})_i");
        if (vertices_alo_amo)
            V().forEach(v -> {
                List<String> list = new ArrayList<>();
                LV_().forEach(i -> {
                    list.add(var("p", i) + " = " + v);
                });
                R().forEach(r -> {
                    list.add(var("p", r, v));
                });
                String vars = list.stream().collect(Collectors.joining(", "));
                clause("ALO(%d, %s)", 2, vars);
                clause("AMO(%d, %s)", m1, vars);
            });
        else
            clause("// Not today!");


        clause("// 2.1.2 Reticular (forall r in R)");

        clause("// ONE(p_{i,r})_i");
        if (reticular_one)
            R().forEach(r -> {
                List<String> list = new ArrayList<>();
                LV_().forEach(i -> {
                    list.add(var("p", i) + " = " + r);
                });
                String vars = list.stream().collect(Collectors.joining(", "));
                // TODO: wtf, how to do ONE() properly?
                clause("ALO(%d, %s)", 1, vars);
                clause("AMO(%d, %s)", 1, vars);
            });
        else
            clause("// Not today!");


        clause("// AL2(p_{r,i})_i  and  AMM2(p_{r,i})_i");
        if (retucular_alo_amo)
            R().forEach(r -> {
                List<String> list = new ArrayList<>();
                PP(r).forEach(i -> {
                    list.add(var("p", r, i));
                });
                String vars = list.stream().collect(Collectors.joining(", "));
                clause("ALO(%d, %s)", 2, vars);
                clause("AMO(%d, %s)", m2, vars);
            });
        else
            clause("// Not today!");


        clause("// 2.1.3 Leaves (forall l in L)");
        // clause("// ONE(p_{l,i})_i");
        // no need
        clause("// no constraints for leaves today!");


        clause("// 2.1.4* <Numeration order>");
        // TODO: check wildly!

        clause("// p_{r,v} and p_{u,r} => (v > u)");
        if (numeration_reticular_through)
            R().forEach(r -> {
                PP(r).forEach(v -> {
                    LV_().forEach(u -> {
                        // clause("%s & (%s = %d) => (%d > %d)",
                        //         var("p", r, v),
                        //         var("p", u), r,
                        //         v, u);
                        if (!(v > u)) {
                            clause("! %s | (%s != %d)",
                                    var("p", r, v),
                                    var("p", u), r);
                        }
                    });
                });
            });
        else
            clause("// Not today!");

        // // p_{i,v} => not p_{u,i}  ~~~  if u > v
        // // TODO: check is it really the same:
        // clause("// (p_i = v) => (p_v > i)   [u in C, v in V]");
        // // (p_i = r) => ALL(p_{r,v} => v > i)_{v in PP(r)}  // ah, see above!
        // LV_().forEach(i -> {
        //     PP(i).forEach(v -> {
        //         if (n + 1 <= v && v < root())
        //             clause("(%s = %d) => (%s > %d)",
        //                     var("p", i), v,
        //                     var("p", v), i);
        //         // LV_().forEach(u -> {
        //         // if (u > v)
        //         //     clause("(%s = %d) => (%s != %d)",
        //         //             var("p", i), v,
        //         //             var("p", u), i);
        //         // });
        //     });
        // });

        clause("// p_{i,v} and p_{u,i} => (v > u)");
        if (numeration_vertices_through)
            V_().forEach(i -> {
                PP(i).forEach(v -> {
                    // LV_().forEach(u -> {
                    //     // clause("(%s = %d) & (%s = %d) => (%d > %d)",
                    //     //         var("p", i), v,
                    //     //         var("p", u), i,
                    //     //         v, u);
                    //     if (!(v > u)) {
                    //         clause("(%s != %d) | (%s != %d)",
                    //                 var("p", i), v,
                    //                 var("p", u), i);
                    //     }
                    // });
                    // it is also holds when {v in R}
                    if (v > root()) {
                        // R().filter(u -> u != v).forEach(u -> {
                        R().forEach(u -> {
                            // clause("(%s = %d) & %s => (%d > %d)",
                            //         var("p", i), v,
                            //         var("p", u, i),
                            //         v, u);
                            if (!(v > u)) {
                                clause("(%s != %d) | ! %s",
                                        var("p", i), v,
                                        var("p", u, i));
                            }
                        });
                    }
                });
            });
        else
            clause("// Not today!");

        clause("// p_{u1,r1} and p_{u2,r2} => ((r1 > r2) <=> (u1 > u2))");
        if (numeration_reticular_order)
            LV_().forEach(u1 -> {
                LV_().forEach(u2 -> {
                    R().forEach(r1 -> {
                        R().forEach(r2 -> {
                            // clause("(%s = %d) & (%s = %d) => ((%d > %d) <=> (%d > %d))",
                            //         var("p", u1), r1,
                            //         var("p", u2), r2,
                            //         r1, r2,
                            //         u1, u2);
                            if (!((r1 > r2) == (u1 > u2))) {
                                clause("(%s != %d) | (%s != %d)",
                                        var("p", u1), r1,
                                        var("p", u2), r2);
                            }
                        });
                    });
                });
            });
        else
            clause("// Not today!");

        // clause("// p_{u1,v1} and p_{u2,v2} and (u1 > u2) => (v1 >= v2)");
        // TODO: check iff
        clause("// p_{u1,v1} and p_{u2,v2} => ((u1 > u2) <=> (v1 >= v2))");
        if (numeration_vertices_order)
            V_().forEach(u1 -> {
                PP(u1).forEach(v1 -> {
                    V_().forEach(u2 -> {
                        PP(u2).forEach(v2 -> {
                            // clause("(%s = %d) & (%s = %d) & (%d > %d) => (%d >= %d)",
                            //         var("p", u1), v1,
                            //         var("p", u2), v2,
                            //         u1, u2,
                            //         v1, v2);
                            if (!((u1 > u2) == (v1 >= v2)))
                                clause("(%s != %d) | (%s != %d)",
                                        var("p", u1), v1,
                                        var("p", u2), v2);
                        });
                    });
                });
            });
        else
            clause("// Not today!");
    }

    private void declareTreesToNetworkMappingConstraints() {
        clause("// 2.2 Trees to network mapping constraints");
        boolean mapping_alo = true;
        boolean mapping_root = true;
        boolean reticular_not = true;
        boolean reticular_used = true;
        boolean reticular_alo = true;
        boolean interrupt_or = false;
        boolean interrupt_iff = true;
        boolean union_iff = true;
        boolean union_impl = true;

        clause("// 2.2.1 Mapping");

        // clause("// ONE(x_{t,v,i})_i");
        // no need?

        clause("// ALO(x_{t,i,v_t})_i");
        if (mapping_alo)
            T().forEach(t -> {
                Vt().forEach(vt -> {
                    List<String> list = new ArrayList<>();
                    VR().forEach(i -> {
                        list.add(var("x", t, i) + " = " + vt);
                    });
                    String vars = list.stream().collect(Collectors.joining(", "));
                    clause("ALO(1, %s)", vars);
                });
            });
        else
            clause("// Not today!");

        clause("// Root mapping");
        if (mapping_root)
            T().forEach(t -> {
                clause("%s = %d", var("x", t, root()), rootT());
            });
        else
            clause("// Not today!");


        clause("// 2.2.2 Reticular <parents>");

        // clause("// ONE(p_{t,r,u})_u");
        // no need

        clause("// not(p_{r,u}) => not(p_{t,r,u})");
        if (reticular_not)
            T().forEach(t -> {
                R().forEach(r -> {
                    PP(r).forEach(u -> {
                        clause("! %s => (%s != %d)",
                                var("p", r, u),
                                var("pt", t, r), u);
                    });
                });
            });
        else
            clause("// Not today!");

        clause("// p_{r,u} and not(p_{t,r,u}) => not(u_{t,u})");
        if (reticular_used)
            T().forEach(t -> {
                R().forEach(r -> {
                    PP(r).forEach(u -> {
                        clause("%s & (%s != %d) => ! %s",
                                var("p", r, u),
                                var("pt", t, r), u,
                                var("u", t, u));
                    });
                });
            });
        else
            clause("// Not today!");

        clause("// p_{r,u} => ALO(p_{t,r,u})_t");
        if (reticular_alo)
            R().forEach(r -> {
                PP(r).forEach(u -> {
                    List<String> list = new ArrayList<>();
                    T().forEach(t -> {
                        list.add(var("pt", t, r) + " = " + u);
                    });
                    String vars = list.stream().collect(Collectors.joining(", "));
                    clause("%s => ALO(1, %s)", var("p", r, u), vars);
                });
            });
        else
            clause("// Not today!");


        clause("// 2.2.3 Mapping interrupt");

        // TODO: checkout report for a more strict definition of right part
        // clause("// u_{t,v} and x_{t,v,v_t} and p_{u,v} => not(x_{t,u,v_t})");
        // for R: u_{t,v} and x_{t,v,v_t} and p_{t,u,v} => not(x_{t,u,v_t})");
        // T().forEach(t -> {
        //     LVt().forEach(vt -> {
        //         // L().forEach(u -> {
        //         //     // int ut = u;  // because it is leaf
        //         //     // x_{t,u} = ut
        //         //     // not(x_{t,u,vt})  ===  x_{t,u} != vt  ===  ut != vt
        //         //     PP(u).forEach(v -> clause("%s & (%s = %d) & (%s = %d) => (%s != %d)",
        //         //             var("u", t, v),
        //         //             var("x", t, v), vt,
        //         //             var("p", u), v,
        //         //             var("x", t, u), vt));
        //         // });
        //         // V_().forEach(u -> {
        //         LV_().forEach(u -> {
        //             PP(u).forEach(v -> clause("%s & (%s = %d) & (%s = %d) => (%s != %d)",
        //                     var("u", t, v),
        //                     var("x", t, v), vt,
        //                     var("p", u), v,
        //                     var("x", t, u), vt));
        //         });
        //         R().forEach(u -> {
        //             PP(u).forEach(v -> clause("%s & (%s = %d) & (%s = %d) => (%s != %d)",
        //                     var("u", t, v),
        //                     var("x", t, v), vt,
        //                     var("pt", t, u), v,
        //                     var("x", t, u), vt));
        //         });
        //     });
        // });

        // More strict right part definition of previous constraint:
        clause("// u_{t,v} and x_{t,v,v_t} and p_{u,v} => OR_{v_t^c}(x_{t,u,v_t^c})");
        // for R:                     ... and p_{t,u,v} => ...
        if (interrupt_or)
            T().forEach(t -> {
                Vt().forEach(vt -> {
                    // L().forEach(u -> {
                    // ut = u, because it is leaf.
                    // x_t_u_ut = true, because it is leaf.
                    // Constraint should be "... => x_t_u_ut", but it is
                    // always true, so we completely do not need that L() loop
                    // })
                    V_().forEach(u -> {
                        PP(u).forEach(v -> clause("%s & (%s = %d) & (%s = %d) => (%s)",
                                var("u", t, v),
                                var("x", t, v), vt,
                                var("p", u), v,
                                getChildrenInTree(t, vt).stream()
                                        .map(vtc -> var("x", t, u) + " = " + vtc)
                                        .collect(Collectors.joining(" | "))));
                    });
                    R().forEach(u -> {
                        PP(u).forEach(v -> clause("%s & (%s = %d) & (%s = %d) => (%s)",
                                var("u", t, v),
                                var("x", t, v), vt,
                                var("pt", t, u), v,
                                getChildrenInTree(t, vt).stream()
                                        .map(vtc -> var("x", t, u) + " = " + vtc)
                                        .collect(Collectors.joining(" | "))));
                    });
                });
            });
        else
            clause("// Not today!");

        // p_{u,v} and x_{t,u,u_t} and u_{t,u} => x_{t,v,v_t}
        // p_{u,v} and x_{t,u,u_t} and x_{t,v,v_t} => u_{t,v}
        // same as:
        clause("// p_{u,v} and x_{t,u,u_t} => (u_{t,v} <=> x_{t,v,v_t})");
        // for R: p_{t,u,v} and x_{t,u,u_t} => (u_{t,v} <=> x_{t,v,v_t})");
        if (interrupt_iff)
            T().forEach(t -> {
                L().forEach(u -> {
                    PP(u).forEach(v -> {
                        int ut = u;  // because it is leaf
                        int vt = getParentInTree(t, ut);
                        // x_{t,u,ut}=true for leaves!
                        clause("(%s = %d) => (%s <=> (%s = %d))",
                                var("p", u), v,
                                var("u", t, v),
                                var("x", t, v), vt);
                    });
                });
                V_().forEach(u -> {
                    PP(u).forEach(v -> {
                        LVt_().forEach(ut -> {
                            int vt = getParentInTree(t, ut);
                            clause("(%s = %d) and (%s = %d) => (%s <=> (%s = %d))",
                                    var("p", u), v,
                                    var("x", t, u), ut,
                                    var("u", t, v),
                                    var("x", t, v), vt);
                        });
                    });
                });
                R().forEach(u -> {
                    PP(u).forEach(v -> {
                        LVt_().forEach(ut -> {
                            int vt = getParentInTree(t, ut);
                            clause("(%s = %d) and (%s = %d) => (%s <=> (%s = %d))",
                                    var("pt", t, u), v,
                                    var("x", t, u), ut,
                                    var("u", t, v),
                                    var("x", t, v), vt);
                        });
                    });
                });
            });
        else
            clause("// Not today!");


        clause("// 2.2.4 Mapping union");

        clause("// not(u_{t,s}) and p_{i,s} => (x_{t,i,v_t} <=> x_{t,s,v_t})");
        // NOTE: with BEE I do not even need v_t
        if (union_iff)
            T().forEach(t -> {
                L().forEach(i -> {
                    PP(i).forEach(s -> {
                        int vt = i;  // because it is leaf
                        // x_{t,i,vt}=true
                        clause("! %s & (%s = %d) => (%s = %d)",
                                // clause("! %s & (%s = %d) => ((%s = %d) <=> (%s = %d))",
                                // clause("! %s & (%s = %d) => (%s = %s)",
                                var("u", t, s),
                                var("p", i), s,
                                var("x", t, s), vt);
                        // var("x", t, i), vt, var("x", t, s), vt);
                        // var("x", t, i), var("x", t, s));
                    });
                });
                V_().forEach(i -> {
                    PP(i).forEach(s -> {
                        clause("! %s & (%s = %d) => (%s = %s)",
                                var("u", t, s),
                                var("p", i), s,
                                var("x", t, i), var("x", t, s));
                    });
                });
                R().forEach(i -> {
                    PP(i).forEach(s -> {
                        clause("! %s & (%s = %d) => (%s = %s)",
                                var("u", t, s),
                                var("pt", t, i), s,
                                var("x", t, i), var("x", t, s));
                    });
                });
            });
        else
            clause("// Not today!");

        clause("// p_{i,s} and x_{t,i,v_t} and x_{t,s,v_t} => not(u_{t,s})");
        // with BEE: p_{i,s} and (x_{t,i} = x_{t,s}) => not(u_{t,s})
        if (union_impl)
            T().forEach(t -> {
                L().forEach(i -> {
                    PP(i).forEach(s -> {
                        int vt = i;  // because it is leaf
                        // x_{t,i,vt}=true
                        clause("(%s = %d) & (%s = %d) => ! %s",
                                var("p", i), s,
                                var("x", t, s), vt,
                                var("u", t, s));
                    });
                });
                V_().forEach(i -> {
                    PP(i).forEach(s -> {
                        clause("(%s = %d) & (%s = %s) => ! %s",
                                var("p", i), s,
                                var("x", t, i), var("x", t, s),
                                var("u", t, s));
                    });
                });
                R().forEach(i -> {
                    PP(i).forEach(s -> {
                        clause("(%s = %d) & (%s = %s) => ! %s",
                                var("pt", t, i), s,
                                var("x", t, i), var("x", t, s),
                                var("u", t, s));
                    });
                });
            });
        else
            clause("// Not today!");


        /* THE MOST HILARIOUS AD-HOCs */
        clause("// Ad-hoc");
        if (hasFictitiousRoot)
            clause("p_0 = " + root());
        T().forEach(t -> {
            clause(var("u", t, root()));
        });
    }


    private List<Integer> makeList(IntStream stream) {
        return stream.boxed().collect(Collectors.toList());
    }

    private IntStream L() {
        /* Leaves */
        if (hasFictitiousRoot)
            return IntStream.rangeClosed(0, n);
        else
            return IntStream.rangeClosed(1, n);
    }

    private IntStream V() {
        /* Vertices */
        return IntStream.rangeClosed(n + 1, root());
    }

    private IntStream V_() {
        /* Vertices without Root */
        return IntStream.range(n + 1, root());
    }

    private IntStream R() {
        /* Reticulars */
        return IntStream.rangeClosed(root() + 1, root() + k);
    }

    private IntStream LV() {
        /* Leaves + Vertices */
        return IntStream.concat(L(), V());
    }

    private IntStream LV_() {
        /* Leaves + Vertices without Root */
        return IntStream.concat(L(), V_());
    }

    private IntStream VR() {
        /* Vertices + Reticulars */
        return IntStream.concat(V(), R());
    }

    private IntStream LVR() {
        /* All nodes */
        return IntStream.concat(IntStream.concat(L(), V()), R());
    }

    private IntStream LVR_() {
        /* All nodes without Root */
        return IntStream.concat(IntStream.concat(L(), V_()), R());
    }

    // FIXME: temporary method addition
    private IntStream LVt() {
        /* Tree vertices and leaves */
        return IntStream.concat(L(), Vt());
    }

    // FIXME: temporary method addition
    private IntStream LVt_() {
        /* Tree vertices and leaves without Root */
        return IntStream.concat(L(), Vt_());
    }

    private IntStream Vt() {
        /* Tree vertices */
        // TODO: what's the end? 2n if fictive and 2n-1 if not???
        // return IntStream.rangeClosed(n + 1, 2 * n);
        return IntStream.rangeClosed(n + 1, rootT());
    }

    private IntStream Vt_() {
        /* Tree vertices without RootT */
        return IntStream.range(n + 1, rootT());
    }

    private IntStream T() {
        /* Trees */
        return IntStream.rangeClosed(0, trees.size() - 1);
    }

    private int rootT() {
        /* Tree root (maybe fictitious) */
        // return 2 * n;
        // FIXME: I don't know what is that and how to calc it properly ._.
        if (hasFictitiousRoot)
            return 2 * n;
        else
            return 2 * n - 1;
    }

    private int root() {
        /* Root */
        if (hasFictitiousRoot)
            return 2 * n + k;
        else
            return 2 * n + k - 1;
    }

    private IntStream PP(int v) {
        if (v < root()) {  // v \in L \cup V \setminus \rho
            return VR().filter(u -> u > v);
        } else if (v > root()) {  // v \in R
            return V();
        } else {  // v = \rho
            throw new IllegalArgumentException("Why are you trying to get root's parent?");
            // return IntStream.empty();
        }
    }

    private int getParentInTree(int t, int ut) {
        if (hasFictitiousRoot && 0 <= ut && ut < rootT()) {
            return trees.get(t).getParent(ut);
        } else if (!hasFictitiousRoot && 0 < ut && ut < rootT()) {
            return trees.get(t).getParent(ut);
        } else
            throw new IllegalArgumentException("Cannot get parent of " + ut);
    }

    private List<Integer> getChildrenInTree(int t, int vt) {
        if (n < vt && vt <= rootT()) {
            return trees.get(t).getChildren(vt);
        } else
            throw new IllegalArgumentException("Cannot get children of " + vt);
    }

    private String var(String prefix, int... params) {
        return prefix + Arrays.stream(params)
                .mapToObj(x -> "_" + x)
                .collect(Collectors.joining());
    }

    private void clause(String clause) {
        clauses.add(clause);
    }

    private void clause(String format, Object... args) {
        clauses.add(String.format(format, args));
    }
}
