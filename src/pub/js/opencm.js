/*Copyright (c) 2013-2016, Rob Schmuecker
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* The name Rob Schmuecker may not be used to endorse or promote products
  derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL MICHAEL BOSTOCK BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

//
/* This software includes portions/modifications by Software AG. */
//

// -------------------------------------------------------------------------------
// NOTE:
// OpenCM expects "cmdata" directory under the pub folder of the package
// To create on Windows: mklink -D LinkName TargetDir
// e.g. mklink /D cmdata \Users\hhansson\Downloads\CM\cmdata
// -------------------------------------------------------------------------------
function getQueryVariable(variable)
{
       var query = window.location.search.substring(1);
       var vars = query.split("&");
       for (var i=0;i<vars.length;i++) {
               var pair = vars[i].split("=");
               if(pair[0] == variable){return pair[1];}
       }
       return(false);
}

var opencm_node = getQueryVariable("node");
var node_page = false;

if ((opencm_node != null) && (opencm_node != "")) {
	node_page = true;
}

var cm_action = getQueryVariable("action");

if (node_page) {
	var json_tree = "dndTree/" + opencm_node + "_RUNTIME.json";			// Always display tree based on runtime.... (baseline may exist as well)
} else {
	var json_tree = "dndTree/overview.json";
}

var cmdataBaselineExists = false;
var cmdataRuntimeExists = false;

function notifyMissingLink(what) {
	$('#notification').html("<p>NOTE: " + what + " link missing from package pub directory.... please define.</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function extractWithProps() {
	if (confirm('Extract runtime information from all environment and nodes defined in extract.properties?')) {
		$.get( "/invoke/OpenCM.pub.dsp.snapshots/initiateSnapshot", { node: "PROPS" } );
		$('#notification').html("<p>Runtime snapshot generation started....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>Extraction cancelled ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}

function extractNode() {
	$.get( "/invoke/OpenCM.pub.dsp.snapshots/initiateSnapshot", { node: opencm_node } );
	$('#notification').html("<p>Runtime extraction started for " + opencm_node + "....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function promoteNode() {
	if (confirm('Promote Node ' + opencm_node + ' to Baseline?')) {
		$.get( "/invoke/OpenCM.pub.dsp.snapshots/promoteSnapshot", { node: opencm_node } );
		$('#notification').html("<p>Node " + opencm_node + " promoted to baseline....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>Node not promoted ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}

function performBaselineSingleAudit() {
	if (cmdataBaselineExists) {
		$.get( "/invoke/OpenCM.pub.dsp.audit/initiateBaselineAudit", { node: opencm_node } );
		$('#notification').html("<p>Baseline vs. Runtime Node " + opencm_node + " Audit Process Started....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>No Baseline data exists - no auditing possible ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}

function performBaselineFullAudit() {
	$.get( "/invoke/OpenCM.pub.dsp.audit/initiateBaselineAudit", { node: "ALL" } );
	$('#notification').html("<p>Full Baseline Audit Process Started....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function performDefaultRuntimeAudit() {
	$.get( "/invoke/OpenCM.pub.dsp.audit/initiateDefaultAudit", { node: opencm_node, repo: "runtime"} );
	$('#notification').html("<p>Default vs. Runtime Node " + opencm_node + " Audit Process Started....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function performDefaultBaselineAudit() {
	if (cmdataBaselineExists) {
		$.get( "/invoke/OpenCM.pub.dsp.audit/initiateDefaultAudit", { node: opencm_node, repo: "baseline"} );
		$('#notification').html("<p>Default vs. Baseline Node " + opencm_node + " Audit Process Started....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>No Baseline data exists - no auditing possible ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}

function performTwoNodeAudit(auditPropertyDir) {
	$.get( "/invoke/OpenCM.pub.dsp.audit/initiateTwoNodeAudit", { auditProp: auditPropertyDir } );
	$('#notification').html("<p>2-Node Audit Process Started for " + auditPropertyDir + " ....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function performLayeredAudit(auditLayeredProperty) {
	$.get( "/invoke/OpenCM.pub.dsp.audit/initiateLayeredAudit", { auditLayeredProp: auditLayeredProperty } );
	$('#notification').html("<p>Layered Audit Process Started for " + auditLayeredProperty + " using Assertion groups....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function performTreeUpdate() {
	$.get( "/invoke/OpenCM.pub.dsp.configuration/updateTree");
	$('#notification').html("<p>Refreshing Treestructure ....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}

function performEncrypt() {
	$.get( "/invoke/OpenCM.pub.dsp.configuration/encrypt");
	$('#notification').html("<p>Encrypting Endpoints ....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}
function performDecrypt() {
	if (confirm('Decrypt all passwords in the nodes.properties file?')) {
		$.get( "/invoke/OpenCM.pub.dsp.configuration/decrypt");
		$('#notification').html("<p>Decrypting Endpoints ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>Decryption cancelled ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}
function performCceRefresh() {
	if (confirm('This action will remove/create all enviornments and nodes in Command Central based on the opencm configuration. Confirm to contine:')) {
		$.get( "/invoke/OpenCM.pub.dsp.configuration/manageCCEnodes", { action: "refreshAll", node: null} );
		$('#notification').html("<p>Creating all nodes and environments within Command Central ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>CCE refresh cancelled ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}
function performCceAddNode() {
	$.get( "/invoke/OpenCM.pub.dsp.configuration/manageCCEnodes", { action: "generateNode", node: opencm_node} );
	$('#notification').html("<p>Creating the " + opencm_node + " node within Command Central ....</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}
function performGenerateCceFixScript() {
	$.get( "/invoke/OpenCM.pub.dsp.configuration/generateCceFixScript", { node: opencm_node} );
	$('#notification').html("<p>Generating Command Central CLI script for Fix installation based on " + opencm_node + " ...</p>");
	$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
}
function performSynchSend() {
	if (confirm('Synchronize runtime extractions and send to target OpenCM component via FTPS?')) {
		$.get( "/invoke/OpenCM.pub.dsp.configuration/synchSend");
		$('#notification').html("<p>Synchronizing runtime data to central OpenCM instance ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	} else {
		$('#notification').html("<p>Synchronization cancelled ....</p>");
		$('#notification').slideToggle(500).delay(3000).fadeToggle(500);
	}
}

// Get JSON data
treeJSON = d3.json(json_tree, function(error, treeData) {
	
    // Calculate total nodes, max label length
    var totalNodes = 0;
    var maxLabelLength = 0;

    // variables for drag/drop
    var selectedNode = null;
    var draggingNode = null;
    // panning variables

    var panSpeed = 200;
    var panBoundary = 20; // Within 20px from edges will pan when dragging.
    // Misc. variables
    var i = 0;

    var duration = 750;
    var root;

    // size of the diagram

    var viewerWidth = ($(document).width() / 4) * 3; // 75%
    //var viewerWidth = $(document).width();
    var viewerHeight = $(document).height();

    var tree = d3.layout.tree()
        .size([viewerHeight, viewerWidth]);

    // define a d3 diagonal projection for use by the node paths later on.
    var diagonal = d3.svg.diagonal()
        .projection(function(d) {
            return [d.y, d.x];
        });
	// To enable toggling of text attributes of previously selected nodes
	var prevNodeText;
		
    // A recursive helper function for performing some setup by walking through all nodes
    function visit(parent, visitFn, childrenFn) {

		if (!parent) return;

        visitFn(parent);

        var children = childrenFn(parent);
        if (children) {
            var count = children.length;
            for (var i = 0; i < 2; i++) {
                visit(children[i], visitFn, childrenFn);
            }
        }
    }
	
    // Call visit function to establish maxLabelLength
    visit(treeData, function(d) {
        totalNodes++;
        maxLabelLength = Math.max(d.name.length, maxLabelLength);

    }, function(d) {
        return d.children && d.children.length > 0 ? d.children : null;
    });


    // sort the tree according to the node names
    function sortTree() {
        tree.sort(function(a, b) {
            return b.name.toLowerCase() < a.name.toLowerCase() ? 1 : -1;
        });
    }
    // Sort the tree initially incase the JSON isn't in a sorted order.
    // sortTree();

    // TODO: Pan function, can be better implemented.

    function pan(domNode, direction) {
        var speed = panSpeed;
        if (panTimer) {
            clearTimeout(panTimer);
            translateCoords = d3.transform(svgGroup.attr("transform"));
            if (direction == 'left' || direction == 'right') {
                translateX = direction == 'left' ? translateCoords.translate[0] + speed : translateCoords.translate[0] - speed;
                translateY = translateCoords.translate[1];
            } else if (direction == 'up' || direction == 'down') {
                translateX = translateCoords.translate[0];
                translateY = direction == 'up' ? translateCoords.translate[1] + speed : translateCoords.translate[1] - speed;
            }
            scaleX = translateCoords.scale[0];
            scaleY = translateCoords.scale[1];
            scale = zoomListener.scale();
            svgGroup.transition().attr("transform", "translate(" + translateX + "," + translateY + ")scale(" + scale + ")");
            d3.select(domNode).select('g.node').attr("transform", "translate(" + translateX + "," + translateY + ")");
            zoomListener.scale(zoomListener.scale());
            zoomListener.translate([translateX, translateY]);
            panTimer = setTimeout(function() {
                pan(domNode, speed, direction);
            }, 50);
        }
    }

    // Define the zoom function for the zoomable tree
    function zoom() {
        svgGroup.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
    }


    // define the zoomListener which calls the zoom function on the "zoom" event constrained within the scaleExtents
    var zoomListener = d3.behavior.zoom().scaleExtent([0.1, 3]).on("zoom", zoom);

    function initiateDrag(d, domNode) {
        draggingNode = d;
		// Disabled ..........
        // d3.select(domNode).select('.ghostCircle').attr('pointer-events', 'none');
        // d3.selectAll('.ghostCircle').attr('class', 'ghostCircle show');
        d3.select(domNode).attr('class', 'node activeDrag');

        svgGroup.selectAll("g.node").sort(function(a, b) { // select the parent and sort the path's
            if (a.id != draggingNode.id) return 1; // a is not the hovered element, send "a" to the back
            else return -1; // a is the hovered element, bring "a" to the front
        });
        // if nodes has children, remove the links and nodes
        if (nodes.length > 1) {
            // remove link paths
            links = tree.links(nodes);
            nodePaths = svgGroup.selectAll("path.link")
                .data(links, function(d) {
                    return d.target.id;
                }).remove();
            // remove child nodes
            nodesExit = svgGroup.selectAll("g.node")
                .data(nodes, function(d) {
                    return d.id;
                }).filter(function(d, i) {
                    if (d.id == draggingNode.id) {
                        return false;
                    }
                    return true;
                }).remove();
        }

        // remove parent link
        parentLink = tree.links(tree.nodes(draggingNode.parent));
        svgGroup.selectAll('path.link').filter(function(d, i) {
            if (d.target.id == draggingNode.id) {
                return true;
            }
            return false;
        }).remove();

        dragStarted = null;
    }

    // define the baseSvg, attaching a class for styling and the zoomListener
    var baseSvg = d3.select("#canvasTree").append("svg")
        .attr("width", viewerWidth)
        .attr("height", viewerHeight)
        // .attr("class", "overlay")
        .call(zoomListener);

    // Define the drag listeners for drag/drop behaviour of nodes.
    dragListener = d3.behavior.drag()
        .on("dragstart", function(d) {

            if (d == root) {
                return;
            }
            dragStarted = true;
            nodes = tree.nodes(d);
            d3.event.sourceEvent.stopPropagation();
            // it's important that we suppress the mouseover event on the node being dragged. Otherwise it will absorb the mouseover event and the underlying node will not detect it d3.select(this).attr('pointer-events', 'none');
        })
        .on("drag", function(d) {
            if (d == root) {
                return;
            }
            if (dragStarted) {
                domNode = this;
                initiateDrag(d, domNode);
            }

            // get coords of mouseEvent relative to svg container to allow for panning
            relCoords = d3.mouse($('svg').get(0));
            if (relCoords[0] < panBoundary) {
                panTimer = true;
                pan(this, 'left');
            } else if (relCoords[0] > ($('svg').width() - panBoundary)) {

                panTimer = true;
                pan(this, 'right');
            } else if (relCoords[1] < panBoundary) {
                panTimer = true;
                pan(this, 'up');
            } else if (relCoords[1] > ($('svg').height() - panBoundary)) {
                panTimer = true;
                pan(this, 'down');
            } else {
                try {
                    clearTimeout(panTimer);
                } catch (e) {

                }
            }

            d.x0 += d3.event.dy;
            d.y0 += d3.event.dx;
            var node = d3.select(this);
            node.attr("transform", "translate(" + d.y0 + "," + d.x0 + ")");
            updateTempConnector();
        }).on("dragend", function(d) {
            if (d == root) {
                return;
            }
            domNode = this;
            endDrag();
			// displayProps(d);
        });

    function endDrag() {
        selectedNode = null;
        d3.selectAll('.ghostCircle').attr('class', 'ghostCircle');
        d3.select(domNode).attr('class', 'node');
        // now restore the mouseover event or we won't be able to drag a 2nd time
        d3.select(domNode).select('.ghostCircle').attr('pointer-events', '');
        updateTempConnector();
        if (draggingNode !== null) {
            update(root);
            centerNode(draggingNode);
            draggingNode = null;
        }
    }

    // Helper functions for collapsing and expanding nodes.

    function collapse(d) {
        if (d.children) {
            d._children = d.children;
            d._children.forEach(collapse);
            d.children = null;
        }
    }

    function expand(d) {
        if (d._children) {
            d.children = d._children;
            d.children.forEach(expand);
            d._children = null;
        }
    }
	
    var overCircle = function(d) {
        selectedNode = d;
        updateTempConnector();
    };
    var outCircle = function(d) {
        selectedNode = null;
        updateTempConnector();
    };

    // Function to update the temporary connector indicating dragging affiliation
    var updateTempConnector = function() {
        var data = [];
        if (draggingNode !== null && selectedNode !== null) {
            // have to flip the source coordinates since we did this for the existing connectors on the original tree
            data = [{
                source: {
                    x: selectedNode.y0,
                    y: selectedNode.x0
                },
                target: {
                    x: draggingNode.y0,
                    y: draggingNode.x0
                }
            }];
        }
        var link = svgGroup.selectAll(".templink").data(data);

        link.enter().append("path")
            .attr("class", "templink")
            .attr("d", d3.svg.diagonal())
            .attr('pointer-events', 'none');

        link.attr("d", d3.svg.diagonal());

        link.exit().remove();
    };

    // Function to center node when clicked/dropped so node doesn't get lost when collapsing/moving with large amount of children.

    function centerNode(source) {
        scale = zoomListener.scale();
        x = -source.y0;
        y = -source.x0;
        x = x * scale + viewerWidth / 3;
        y = y * scale + viewerHeight / 2;
        d3.select('g').transition()
            .duration(duration)
            .attr("transform", "translate(" + x + "," + y + ")scale(" + scale + ")");
        zoomListener.scale(scale);
        zoomListener.translate([x, y]);
    }

    // Toggle children function
    function toggleChildren(d) {
        if (d.children) {
            d._children = d.children;
            d.children = null;
        } else if (d._children) {
            d.children = d._children;
            d._children = null;
        }
        return d;
    }

    // Toggle (and center) children on click.
    function opencm_click(d) {
		if (prevNodeText != null) {
			prevNodeText
			.style("fill", "black")
			.style("font-weight", "");
		}
		prevNodeText = d3.select(this).select("text");
		if (d3.event.defaultPrevented) return; // click suppressed
		if (d.parent != null) {
			d = toggleChildren(d);
			update(d);
		}
		if (node_page) {
			if ((d.level == "NODE") || (d.level == "COMPONENT")) {
			  centerNode(d);
			}
			if (d.level == "INSTANCE") {
				d3.select(this).select("text").style("fill", "blue").style("font-weight", "bold");
				d3.select(this).select("circle").style("fill", "blue");
			}
		} else {
			if (d.level == "ASS_GROUP") {
				if (d.children != null) {
					d.children.forEach(function(d) {
						expand(d);
					});
					update(d);
				}
			}
			if ((d.level == "ROOT") || (d.level == "ENVIRONMENT") || (d.level == "ASS_GROUP")) {
				centerNode(d);
			}
		}
		displayProps(d);
    }

    function update(source) {
        // Compute the new height, function counts total children of root node and sets tree height accordingly.
        // This prevents the layout looking squashed when new nodes are made visible or looking sparse when nodes are removed
        // This makes the layout more consistent.
        var levelWidth = [1];
        var childCount = function(level, n) {

            if (n.children && n.children.length > 0) {
                if (levelWidth.length <= level + 1) levelWidth.push(0);

                levelWidth[level + 1] += n.children.length;
                n.children.forEach(function(d) {
                    childCount(level + 1, d);
                });
            }
        };
        childCount(0, root);
        var newHeight = d3.max(levelWidth) * 40; // 40 pixels per line  
        tree = tree.size([newHeight, viewerWidth]);

        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse(),
            links = tree.links(nodes);

        // Set widths between levels based on maxLabelLength.
        nodes.forEach(function(d) {
            // d.y = (d.depth * (maxLabelLength * 15)); //maxLabelLength * 15px
            // alternatively to keep a fixed scale one can set a fixed depth per level
            // Normalize for fixed-depth by commenting out below line
			if (node_page) {
				if (d.parent == null) {
					d.y = (d.depth * 200); //300px per level.
				} else if (d.level == "COMPONENT") {
					d.y = (d.depth * 250); 
				} else {
					d.y = (d.depth * 200); 
				}
			} else {
				if (d.parent == null) {
					d.y = (d.depth * 100); //100px per level.
				} else if (d.level == "ENVIRONMENT") {
					d.y = (d.depth * 200); 
				} else if (d.level == "ASS_GROUP") {
					d.y = (d.depth * 250); 
				} else if (d.level == "SERVER") {
					d.y = (d.depth * 250); 
				} else {
					d.y = (d.depth * 200); 
				}
			}
			
        });

        // Update the nodes…
        node = svgGroup.selectAll("g.node")
            .data(nodes, function(d) {
                return d.id || (d.id = ++i);
            });

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("g")
            .call(dragListener)
            .attr("class", "node")
            .attr("transform", function(d) {
                return "translate(" + source.y0 + "," + source.x0 + ")";
            })
			.on("mouseup", opencm_click);

        nodeEnter.append("circle")
            .attr('class', 'nodeCircle')
            .attr("r", 0)
            .style("fill", function(d) {
                return d._children ? "lightsteelblue" : "#fff";
            });

		nodeEnter.append("text")
            .attr("x", function(d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("dy", ".35em")
            //.attr('class', 'nodeText')
            .attr("text-anchor", function(d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function(d) {
                return d.name;
            })
            .style("fill-opacity", 0)
            .style("fill", function(d) {
              if (!node_page && (d.level == "NODE") && !d.hasRuntime) {
					return "red"; 
			  }
			})
			.attr("class", function(d) {
				if (!node_page && (d.level == "NODE")) { return 'overviewNode'; } 
				if (d.parent == null) { return 'nodeLevel1'; 
				} else if ((d.level == "NODE") || (d.level == "ENVIRONMENT") || (d.level == "ASS_GROUP")) { return 'nodeLevel2';  
				} else if (d.level == "COMPONENT") { return 'nodeLevel3'; 
				} else  {return 'nodeText'; }
			});
			
        // phantom node to give us mouseover in a radius around it
        nodeEnter.append("circle")
            .attr('class', 'ghostCircle')
            .attr("r", 30)
            .attr("opacity", 0.2) // change this to zero to hide the target area
			.style("fill", "red")
            .attr('pointer-events', 'mouseover')
            .on("mouseover", function(node) {
                overCircle(node);
            })
            .on("mouseout", function(node) {
                outCircle(node);
            });
		
        // Set text before or after node to reflect whether node has children or not.
        node.select('text')
            .attr("x", function(d) {
                return d.children || d._children ? -10 : 10;
            })
            .attr("text-anchor", function(d) {
                return d.children || d._children ? "end" : "start";
            })
            .text(function(d) {
                return d.name;
            });

        // Change the circle fill depending on whether it has children and is collapsed
        node.select("circle.nodeCircle")
            .attr("r", 4.5)
            .style("fill", function(d) {
                return d._children ? "lightsteelblue" : "#fff";
            });
			
        // Transition nodes to their new position.
        var nodeUpdate = node.transition()
            .duration(duration)
            .attr("transform", function(d) {
                return "translate(" + d.y + "," + d.x + ")";
            });

			// Fade the text in
        nodeUpdate.select("text")
            .style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition()
            .duration(duration)
            .attr("transform", function(d) {
                return "translate(" + source.y + "," + source.x + ")";
            })
            .remove();

        nodeExit.select("circle")
            .attr("r", 0);

        nodeExit.select("text")
            .style("fill-opacity", 0);

        // Update the links…
        var link = svgGroup.selectAll("path.link")
            .data(links, function(d) {
                return d.target.id;
            });

        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g")
            .attr("class", "link")
            .attr("d", function(d) {
                var o = {
                    x: source.x0,
                    y: source.y0
                };
                return diagonal({
                    source: o,
                    target: o
                });
            });

        // Transition links to their new position.
        link.transition()
            .duration(duration)
            .attr("d", diagonal);

        // Transition exiting nodes to the parent's new position.
        link.exit().transition()
            .duration(duration)
            .attr("d", function(d) {
                var o = {
                    x: source.x,
                    y: source.y
                };
                return diagonal({
                    source: o,
                    target: o
                });
            })
            .remove();

        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
		
    }

	
    function getPath(d) {
		var nodePath = d.name;
		nodeParent = d.parent;
		while (nodeParent.parent) {
			nodePath = nodeParent.name + "/" + nodePath;
			nodeParent = nodeParent.parent;
		}
		return nodeParent.name + "/" + nodePath;
    }

	function readProps(propsPath){
		var props = []; 
		$.getJSON(propsPath, function(json) {
			props = json;
		})
		.fail(function() {
			props = null;
		});
		return props;
	}
	
	function readProps(propsPath, callback) {
		$.getJSON(propsPath, callback);
	}

	function checkCmdataDir(node) {
		var x = $.getJSON("cmdata/baseline/" + node + "/properties.json", function() {
			cmdataBaselineExists = true;
		})
		.fail(function() {
			cmdataBaselineExists = false;
		});
		var x = $.getJSON("cmdata/runtime/" + node + "/properties.json", function() {
			cmdataRuntimeExists = true;
		})
		.fail(function() {
			cmdataRuntimeExists = false;
		});
	}
	
	function populateServerInfo() {
		var server_properties = "cmdata/runtime/" + opencm_server + "/properties.json";		// Always based on runtime
		readProps(server_properties, function(json) {
			if (json.displayName == null) {
				// Verson 9.0 different structure in json/xml
				$('#cm_os_name').empty().append(json.platform.displayName);
				$('#cm_os_code').empty().append(json.platform.code);
				$('#cm_os_version').empty().append(json.platform.version);
				$('#cm_server_cpuCores').empty();
			} else {
				$('#cm_os_name').empty().append(json.displayName);
				$('#cm_os_code').empty().append(json.code);
				$('#cm_os_version').empty().append(json.version);
				$('#cm_server_cpuCores').empty().append(json.cpuCores);
			    $('#cm_server_extractAlias').empty().append(json.extractAlias);
			}
		});
		

	}
	function populateNodeInfo() {
		var node_properties = "cmdata/runtime/" + opencm_node + "/properties.json";		// Always based on runtime
		readProps(node_properties, function(json) {
			// Verson 9.0 different structure in json/xml
			if (json.platform != null) {
				$('#cm_os_name').empty().append(json.platform.displayName);
				$('#cm_os_code').empty().append(json.platform.code);
				$('#cm_os_version').empty().append(json.platform.version);
				$('#cm_server_cpuCores').empty().append(json.platform.code); // Not cores, but code instead...
			} else {
				$('#cm_os_name').empty().append(json.displayName);
				$('#cm_os_code').empty().append(json.code);
				$('#cm_os_version').empty().append(json.version);
				$('#cm_server_cpuCores').empty().append(json.cpuCores);
			}
			$('#cm_server_hostname').empty().append(json.hostname);
			$('#cm_node_name').empty().append(opencm_node);
			$('#cm_node_installtime').empty().append(json.nodeInfo.products.product.installTime);
			$('#cm_node_version').empty().append(json.nodeInfo.products.product.version);
			$('#cm_node_ext_date').empty().append(json.extractionDate);
			$('#cm_server_extractAlias').empty().append(json.extractAlias);
		});
	}
	
	function populateComponentInfo(d) {
		var comp_properties = "cmdata/runtime/" + getPath(d) + "/properties.json";
		readProps(comp_properties, function(json) {
			$('#cm_comp_name').empty().append(json.displayName);
			$('#cm_comp_id').empty().append(json.id);
			$('#cm_comp_pid').empty().append(json.productId);
		});
	}
	function populateInstanceInfo(d) {
		var inst_properties = "cmdata/runtime/" + getPath(d) + "/properties.json";
		readProps(inst_properties, function(json) {
			$('#cm_inst_name').empty().append(json.displayName);
			$('#cm_inst_id').empty().append(json.id);
			$('#cm_inst_type_id').empty().append(json.configurationTypeId);
			$('#cm_inst_runtime_id').empty().append(json.runtimeComponentId);
		});
	}
	function populateBaselineProperties(d) {
		if (cmdataBaselineExists) {
			var ci_properties = "cmdata/baseline/" + getPath(d) + "/ci-properties.json";
			readProps(ci_properties, function(json) {
				$('#cmdata-baseline-content').empty();
				$('#cmdata-baseline-content').jsonView(json);
			});
		} else {
			$('#cmdata-baseline-content').empty();
			$('#cmdata-baseline-content').text("No Baseline Data Exists in Repository");
		}
	}
	function populateRuntimeProperties(d) {
		var ci_properties = "cmdata/runtime/" + getPath(d) + "/ci-properties.json";
		readProps(ci_properties, function(json) {
			$('#cmdata-runtime-content').empty();
			$('#cmdata-runtime-content').jsonView(json);
		});
	}

    function displayProps(d) {

		if (d.url != null) {
			document.location.href = "?" + d.url;
			return;
		}
		
		if (!node_page) {
			return;
		}
			
		// No cmdata available....
		if (root.children == null) {
			return;
		}

		$('#selection-server-trigger').show();
		$('#server-details').show();
		$('#selection-node-trigger').show();
		$('#node-details').show();
		
		if (d.level == "NODE") {
			$('#selection-comp-trigger').hide();
			$('#comp-details').hide();
			$('#selection-inst-trigger').hide();
			$('#inst-details').hide();
			$('#selection-cmdata-baseline-properties').hide();
			$('#cmdata-baseline-content').hide();
			$('#selection-cmdata-runtime-properties').hide();
			$('#cmdata-runtime-content').hide();
		}
		if (d.level == "COMPONENT") {
			populateComponentInfo(d);
			$('#selection-comp-trigger').show();
			$('#comp-details').show();
			$('#selection-inst-trigger').hide();
			$('#inst-details').hide();
			$('#selection-cmdata-baseline-properties').hide();
			$('#cmdata-baseline-content').hide();
			$('#selection-cmdata-runtime-properties').hide();
			$('#cmdata-runtime-content').hide();
		}
		if (d.level == "INSTANCE") {
			populateComponentInfo(d.parent);
			populateInstanceInfo(d);
			populateBaselineProperties(d);
			populateRuntimeProperties(d);
			$('#selection-comp-trigger').show();
			$('#comp-details').show();
			$('#selection-inst-trigger').show();
			$('#inst-details').show();
			$('#selection-cmdata-baseline-properties').show();
			$('#cmdata-baseline-content').show();
			$('#selection-cmdata-runtime-properties').show();
			$('#cmdata-runtime-content').show();
			// .style("font-weight", function(d) { return d.selected ? "bold" : ""; })
		}
	}
	
    function intializeDetails(source) {
		// Hide Notification area
		$('#notification').hide();
		
		if (node_page) {
			$('#opencm_page').text(opencm_node);
			$('#ass_group').text(root.assertionGroup);
			$('#env').text(root.environment);
			$('#spm_url').text(root.spmURL);
		
			if (root.children != null) {
				populateNodeInfo();
				$('#no-cmdata-details').hide();
				$('#cm_no_cmdata').hide();
			} else {
				$('#no-cmdata-details').show();
				$('#cm_no_cmdata').show();
			}
		} else {
			$('#opencm_page').text("OpenCM Installation Nodes Inventory");
			$('#opencm-node-details').hide();
			$('#no-cmdata-details').hide();
			$('#cm_no_cmdata').hide();
		}

		// Hide All Other Properties (server and node info will be delayed (asynch call to read properties)
		$('#selection-server-trigger').hide();
		$('#server-details').hide();
		$('#selection-node-trigger').hide();
		$('#node-details').hide();
		$('#selection-comp-trigger').hide();
		$('#comp-details').hide();
		$('#selection-inst-trigger').hide();
		$('#inst-details').hide();
		$('#selection-cmdata-baseline-properties').hide();
		$('#selection-cmdata-runtime-properties').hide();

		if (node_page) {
			$('#extractNodeMenu').show();
			$('#promoteRuntimeMenu').show();
			$('#audit_PerformBaselineSingleAudit').show();
			$('#audit_PerformDefaultRuntimeAudit').show();
			$('#audit_PerformDefaultBaselineAudit').show();
			$('#conf_CceAddNode').show();
			$('#conf_CceFixScript').show();
		} else {
			$('#extractNodeMenu').hide();
			$('#promoteRuntimeMenu').hide();
			$('#audit_PerformBaselineSingleAudit').hide();
			$('#audit_PerformDefaultRuntimeAudit').hide();
			$('#audit_PerformDefaultBaselineAudit').hide();
			$('#conf_CceAddNode').hide();
			$('#conf_CceFixScript').hide();
		}
    }

    // Append a group which holds all nodes and which the zoom Listener can act upon.
    var svgGroup = baseSvg.append("g");

    // Define the root
    root = treeData;
    root.x0 = viewerHeight / 2;
    root.y0 = 0;

    // Layout the tree initially and center on the root node.
	if (root.children != null) {
		root.children.forEach(function(d) {
			collapse(d);
		});
	}
	
    update(root);
    centerNode(root);
	intializeDetails(root);

	if (node_page) {
		checkCmdataDir(opencm_node);
	}
	
});
(function (x) {
	var y = x("#aboutPopup");
	x(function () {
		x("body").on("click", ".aboutMenuLink", function () {
			var h = x(".aboutFrame"),
			j = x(this);
			y.bPopup(j.data("bpopup") || {})
		})
	});
})(jQuery);
(function (a) {
	var n = a("#auditReport");
	a(function () {
		a("body").on("click", ".auditTwoNodeMenuLink", function () {
			var h = a(".auditReportFrame"),
			j = a(this);
			n.bPopup(j.data("bpopup") || {})
		})
	});
})(jQuery);
(function (a) {
	var n = a("#assertPopup");
	a(function () {
		a("body").on("click", ".assertMenuLink", function () {
			var h = a(".assertFrame"),
			j = a(this);
			n.bPopup(j.data("bpopup") || {})
		})
	});
})(jQuery);
(function (a) {
	var n = a("#inventoryPopup");
	a(function () {
		a("body").on("click", ".inventoryMenuLink", function () {
			var h = a(".inventoryFrame"),
			j = a(this);
			n.bPopup(j.data("bpopup") || {})
		})
	});
})(jQuery);

(function (c) {
	c.fn.bPopup = function (A, E) {
		function L() {
			a.contentContainer = c(a.contentContainer || b);
			switch (a.content) {
			case "iframe":
				var d = c('<iframe class="b-iframe" ' + a.iframeAttr + "></iframe>");
				d.appendTo(a.contentContainer);
				t = b.outerHeight(!0);
				u = b.outerWidth(!0);
				B();
				d.attr("src", a.loadUrl);
				labc(a.loadCallback);
				break;
			default:
				B(),
				c('<div class="b-ajax-wrapper"></div>').load(a.loadUrl, a.loadData, function (d, b, e) {
					labc(a.loadCallback, b);
					F(c(this))
				}).hide().appendTo(a.contentContainer)
			}
		}
		function B() {
			a.modal && c('<div class="b-modal ' + e + '"></div>').css({
				backgroundColor: a.modalColor,
				position: "fixed",
				top: 0,
				right: 0,
				bottom: 0,
				left: 0,
				opacity: 0,
				zIndex: a.zIndex + v
			}).appendTo(a.appendTo).fadeTo(a.speed, a.opacity);
			C();
			b.data("bPopup", a).data("id", e).css({
				left: "slideIn" == a.transition || "slideBack" == a.transition ? "slideBack" == a.transition ? f.scrollLeft() + w : -1 * (x + u) : m(!(!a.follow[0] && n || g)),
				position: a.positionStyle || "absolute",
				top: "slideDown" == a.transition || "slideUp" == a.transition ? "slideUp" == a.transition ? f.scrollTop() + y : z + -1 * t : p(!(!a.follow[1] && q || g)),
				"z-index": a.zIndex + v + 1
			}).each(function () {
				a.appending && c(this).appendTo(a.appendTo)
			});
			G(!0)
		}
		function r() {
			a.modal && c(".b-modal." + b.data("id")).fadeTo(a.speed, 0, function () {
				c(this).remove()
			});
			a.scrollBar || c("html").css("overflow", "auto");
			c(".b-modal." + e).unbind("click");
			f.unbind("keydown." + e);
			k.unbind("." + e).data("bPopup", 0 < k.data("bPopup") - 1 ? k.data("bPopup") - 1 : null);
			b.undelegate(".bClose, ." + a.closeClass, "click." + e, r).data("bPopup", null);
			clearTimeout(H);
			G();
			return !1
		}
		function I(d) {
			y = k.height();
			w = k.width();
			h = D();
			if (h.x || h.y)
				clearTimeout(J), J = setTimeout(function () {
						C();
						d = d || a.followSpeed;
						var e = {};
						h.x && (e.left = a.follow[0] ? m(!0) : "auto");
						h.y && (e.top = a.follow[1] ? p(!0) : "auto");
						b.dequeue().each(function () {
							g ? c(this).css({
								left: x,
								top: z
							}) : c(this).animate(e, d, a.followEasing)
						})
					}, 50)
		}
		function F(d) {
			var c = d.width(),
			e = d.height(),
			f = {};
			a.contentContainer.css({
				height: e,
				width: c
			});
			e >= b.height() && (f.height = b.height());
			c >= b.width() && (f.width = b.width());
			t = b.outerHeight(!0);
			u = b.outerWidth(!0);
			C();
			a.contentContainer.css({
				height: "auto",
				width: "auto"
			});
			f.left = m(!(!a.follow[0] && n || g));
			f.top = p(!(!a.follow[1] && q || g));
			b.animate(f, 250, function () {
				d.show();
				h = D()
			})
		}
		function M() {
			k.data("bPopup", v);
			b.delegate(".bClose, ." + a.closeClass, "click." + e, r);
			a.modalClose && c(".b-modal." + e).css("cursor", "pointer").bind("click", r);
			N || !a.follow[0] && !a.follow[1] || k.bind("scroll." + e, function () {
				if (h.x || h.y) {
					var d = {};
					h.x && (d.left = a.follow[0] ? m(!g) : "auto");
					h.y && (d.top = a.follow[1] ? p(!g) : "auto");
					b.dequeue().animate(d, a.followSpeed, a.followEasing)
				}
			}).bind("resize." + e, function () {
				I()
			});
			a.escClose && f.bind("keydown." + e, function (a) {
				27 == a.which && r()
			})
		}
		function G(d) {
			function c(e) {
				b.css({
					display: "block",
					opacity: 1
				}).animate(e, a.speed, a.easing, function () {
					K(d)
				})
			}
			switch (d ? a.transition : a.transitionClose || a.transition) {
			default:
				b.stop().fadeTo(a.speed, d ? 1 : 0, function () {
					K(d)
				})
			}
		}
		function K(d) {
			d ? (M(), labc(E), a.autoClose && (H = setTimeout(r, a.autoClose))) : (b.hide(), labc(a.onClose), a.loadUrl && (a.contentContainer.empty(), b.css({
						height: "auto",
						width: "auto"
					})))
		}
		function m(a) {
			return a ? x + f.scrollLeft() : x
		}
		function p(a) {
			return a ? z + f.scrollTop() : z
		}
		function labc(a, e) {
			c.isFunction(a) && a.call(b, e)
		}
		function C() {
			z = q ? a.position[1] : Math.max(0, (y - b.outerHeight(!0)) / 2 - a.amsl);
			x = n ? a.position[0] : (w - b.outerWidth(!0)) / 2;
			h = D()
		}
		function D() {
			return {
				x: w > b.outerWidth(!0),
				y: y > b.outerHeight(!0)
			}
		}
		c.isFunction(A) && (E = A, A = null);
		var a = c.extend({}, c.fn.bPopup.defaults, A);
		a.scrollBar || c("html").css("overflow", "hidden");
		var b = this,
		f = c(document),
		k = c(window),
		y = k.height(),
		w = k.width(),
		N = /OS 6(_\d)+/i.test(navigator.userAgent),
		v = 0,
		e,
		h,
		q,
		n,
		g,
		z,
		x,
		t,
		u,
		J,
		H;
		b.close = function () {
			r()
		};
		b.reposition = function (a) {
			I(a)
		};
		return b.each(function () {
			c(this).data("bPopup") || (labc(a.onOpen), 
			v = (k.data("bPopup") || 0) + 1, 
			e = "__b-popup" + v + "__", 
			q = "auto" !== a.position[1], 
			n = "auto" !== a.position[0], 
			g = "fixed" === a.positionStyle, 
			t = b.outerHeight(!0), 
			u = b.outerWidth(!0), 
			a.loadUrl ? L() : B())
		});
	};
	c.fn.bPopup.defaults = {
		amsl: 50,
		appending: !0,
		appendTo: "body",
		autoClose: !1,
		closeClass: "b-close",
		content: "ajax",
		contentContainer: !1,
		easing: "swing",
		escClose: !0,
		follow: [!0, !0],
		followEasing: "swing",
		followSpeed: 500,
		iframeAttr: 'scrolling="no" frameborder="0"',
		loadCallback: !1,
		loadData: !1,
		loadUrl: !1,
		modal: !0,
		modalClose: !0,
		modalColor: "#000",
		onClose: !1,
		onOpen: !1,
		opacity: .7,
		position: ["auto", "auto"],
		positionStyle: "absolute",
		scrollBar: !0,
		speed: 250,
		transition: "fadeIn",
		transitionClose: !1,
		zIndex: 9997
	}
})(jQuery);

// Inventory functions
function getInventoryData(scope) {
	var opencmData = Array();
	if (scope == "environments") {
		$.each(opencm_nodes, function( index, env ) {
			opencmData.push(env.name);
		});
	} else if (scope == "layers") {
		$.each(opencm_nodes, function( index, env ) {
			$.each(env.layers, function( index, layer ) {
				if ($.inArray(layer.name, opencmData) == -1) {
					opencmData.push(layer.name);
				}
			});
		});
	} else if (scope == "nodes") {
		$.each(opencm_nodes, function( index, env ) {
			$.each(env.layers, function( index, layer ) {
				$.each(layer.servers, function( index, server ) {
					$.each(server.nodes, function( index, node ) {
						opencmData.push( node.name );
					});
				});
			});
		});
	} else if (scope == "overview") {
		$.each(opencm_nodes, function( index, env ) {
			$.each(env.layers, function( index, layer ) {
				$.each(layer.servers, function( index, server ) {
					$.each(server.nodes, function( index, node ) {
						opencmData.push([ server.name, node.name ]);
					});
				});
			});
		});
	} else if (scope == "inventory") {
		$.each(opencm_nodes, function( index, env ) {
			$.each(env.layers, function( index, layer ) {
				$.each(layer.servers, function( index, server ) {
					$.each(server.nodes, function( index, node ) {
						var rcs = Array();	// Runtime Components
						$.each(node.rcs, function( index, rc ) {
							rcs.push( {name: rc.name, protocol: rc.protocol, port: rc.port, username: rc.username } );
						});
						opencmData.push( { env: env.name, 
										   layer: layer.name, 
										   server: server.name, 
										   node: node.name,
										   rcs: rcs
										   });
						
					});
				});
			});
		});
	}
	
	return opencmData;
}

if (node_page) {
	$('#nodesTableDiv').hide();
} else {
	$(document).ready(function() {
		$('#all_nodes').DataTable( {
			data: getInventoryData("overview"),
			ordering: false,
			info:     false,
			paging:   true,
			filter:   true,
			pageLength: 25,
			dom: '<"top"f>rt<"bottom"p>',
			columns: [
				{ title: "Hostname" },
				{ title: "Node Name",
				  render: function(data, type, row, meta){
					return '<a target="_top" href="/OpenCM/?node='+data+'">' + data + '</a>';
				  }
				}
			],
			columnDefs: [
				{ 
				targets: '_all',
				className: 'dt-head-left' }
			]
		} );
		
	}); 
}

