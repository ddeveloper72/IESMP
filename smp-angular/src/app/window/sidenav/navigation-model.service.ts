import {MatTreeNestedDataSource} from "@angular/material/tree";
import {Injectable} from "@angular/core";
import {SecurityService} from "../../security/security.service";
import {SecurityEventService} from "../../security/security-event.service";
import {SmpConstants} from "../../smp.constants";
import {HttpClient} from "@angular/common/http";
import {User} from "../../security/user.model";
import {NavigationEnd, Router} from "@angular/router";
import {Observable, Subject} from "rxjs";
import {filter, map} from "rxjs/operators";

/**
 * The smp navigation tree
 */

let PUBLIC_NAVIGATION_TREE: NavigationNode = {
  code: "home",
  name: "Home",
  icon: "home",
  routerLink: "",
  children: [
    {
      code: "search-tools",
      name: "Search",
      icon: "search",
      tooltip: "Search tools",
      routerLink: "public",
      children: [
        {
          code: "search-resources",
          name: "Resources",
          icon: "find_in_page",
          tooltip: "Search registered resources",
          routerLink: "search-resources",

        }
      ]
    }
  ]
};


/**
 * Navigation  data with nested structure.
 * Each node has a name and an optional list of children.
 */
export interface NavigationNode {
  code: string;
  name: string;
  icon?: string;
  tooltip?: string;
  routerLink?: string;
  children?: NavigationNode[];
  selected?: boolean;
  transient?: boolean; // if true then node must be ignored
}

@Injectable()
export class NavigationService extends MatTreeNestedDataSource<NavigationNode> {

  private sub = this.router.events
    .pipe(
      filter(event => event instanceof NavigationEnd),
      map(event => event as NavigationEnd),  // appease typescript
    )
    .subscribe(
      event => {
        console.log('NavigationService: ' + event.url)
        let path: string[] = event.url.split('/');
        this.setNavigationTreeByPath(path, this.rootNode);
      }
    );


  private selectedPathSubject = new Subject<NavigationNode[]>();
  selected: NavigationNode;

  previousSelected: NavigationNode;

  selectedPath: NavigationNode[];

  private rootNode: NavigationNode = PUBLIC_NAVIGATION_TREE;
  private userDetailsNode: NavigationNode = null;


  constructor(protected securityService: SecurityService,
              protected securityEventService: SecurityEventService,
              protected http: HttpClient,
              protected router: Router) {
    super();
    // set  tree data.
    this.refreshNavigationTree();
    // refresh navigation tree on login/logout even types
    securityEventService.onLoginSuccessEvent().subscribe(value => {
        this.refreshNavigationTree();
      }
    );
    securityEventService.onLogoutSuccessEvent().subscribe(value => {
        this.refreshNavigationTree();
      }
    );
    securityEventService.onLogoutErrorEvent().subscribe(value => {
        this.refreshNavigationTree();
      }
    );
  }

  ngOnDestroy() {
    console.log('>> STOP listening to route events ');
    this.sub.unsubscribe();
  }

  select(node: NavigationNode) {

    let targetNode = this.findLeaf(node);
    if (targetNode === this.selected) {
      console.log("Already selected skip");
      return
    }
    if (!!targetNode) {
      if (this.selected) {
        // unselect current value
        this.selected.selected = false;
      }
      this.previousSelected = this.selected;
      this.selected = targetNode
      this.selected.selected = true;
      this.selectedPath = this.findPathForNode(this.selected, this.rootNode);
      this.selectedPathSubject.next(this.selectedPath);
      let navigationPath: string[] = this.getNavigationPath(this.selectedPath);
      // navigate to selected path

      this.router.navigate(navigationPath);
    } else {
      this.selectedPathSubject.next(null);
    }
  }

  selectPreviousNode() {
    this.select(this.previousSelected)
  }


  public reset() {
    this.rootNode = PUBLIC_NAVIGATION_TREE;
    this.data = this.rootNode.children;
    this.select(this.rootNode)

  }


  protected getNavigationPath(path: NavigationNode[]): string [] {
    return path.map(node => node.routerLink);
  }

  protected findLeaf(targetNode: NavigationNode): NavigationNode {
    if (this.noTargetChildren(targetNode)) {
      return targetNode;
    }

    let newTargetNode = targetNode.children[0]
    return this.findLeaf(newTargetNode);
  }

  protected noTargetChildren(targetNode: NavigationNode): boolean {
    if (!targetNode || !targetNode.children || targetNode.children.length == 0) {
      return true;
    }

    let nonTransient = targetNode.children.filter(node => !node.transient);
    return nonTransient.length == 0;
  }

  /**
   * Find vertical path as example [root, parent, target node] for the target node
   * @param targetNode the target node
   * @param parentNode - the root of the tree to start search
   */
  protected findPathForNode(targetNode: NavigationNode, parentNode: NavigationNode): NavigationNode[] {
    if (parentNode === targetNode) {
      return [parentNode];
    }
    if (!parentNode.children) {
      return null;
    }

    const index = parentNode.children.indexOf(targetNode);
    if (index > -1) {
      // got target return initial array
      return [parentNode, targetNode];
    }

    for (const child of parentNode.children) {
      let result = this.findPathForNode(targetNode, child);
      if (result) {
        return [parentNode, ...result];
      }
    }
    return null;
  }

  protected findNodeByCode(nodeCode: string, parentNode: NavigationNode): NavigationNode {
    if (!parentNode.children) {
      return null;
    }
    console.log("find " + nodeCode + " from parent: " + parentNode.code)
    return parentNode.children.find(node => node.routerLink == nodeCode);
  }

  /**
   * Refresh navigation tree for user
   */
  public refreshNavigationTree() {
    this.securityService.isAuthenticated(false).subscribe((isAuthenticated: boolean) => {
      console.log("Refresh application configuration is authenticated " + isAuthenticated)
      if (!isAuthenticated) {
        this.reset();
      } else {

        const currentUser: User = this.securityService.getCurrentUser();
        // get navigation for user
        let navigationObserver = this.http.get<NavigationNode>(SmpConstants.REST_PUBLIC_USER_NAVIGATION_TREE.replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId));

        navigationObserver.subscribe((userRootNode: NavigationNode) => {
          this.setNavigationTree(userRootNode)
        }, (error: any) => {
          // check if unauthorized
          // just console try latter
          console.log("Error occurred while retrieving the navigation model for the user[" + error + "]");
        });
      }
    });
  };

  setNavigationTree(userRootNode: NavigationNode) {
    // find the node by the navigation
    let path: string[] = this.router.url.split('/');
    this.setNavigationTreeByPath(path, userRootNode)
  }

  setNavigationTreeByPath(path: string[], userRootNode: NavigationNode) {
    // find the node by the navigation

    let startNode = userRootNode;

    for (let index in path) {
      let pathSegment = path[index];
      // the first node is empty - skip all empty nodes
      if (!!pathSegment) {
        startNode = this.findNodeByCode(path[index], startNode);
        if (startNode == null) {
          break;
        }
      }
    }

    this.rootNode = userRootNode;
    this.data = this.rootNode?.children;
    this.select(startNode);
  }


  getSelectedPathObservable(): Observable<NavigationNode[]> {
    return this.selectedPathSubject.asObservable();
  }


  /** Add node as child of parent */
  public add(node: NavigationNode, parent: NavigationNode) {
    // add root node
    //const rootNode = {code: "home", name: "Home", icon: "home", children: this.data};
    this._add(node, parent, this.rootNode);
    this.data = this.rootNode.children;
  }

  /** Remove node from tree */
  public remove(node: NavigationNode) {
    const newTreeData = {code: "home", name: "Home", icon: "home", children: this.data};
    this._remove(node, newTreeData);
    this.data = newTreeData.children;
  }

  /*
   * For immutable update patterns, have a look at:
   * https://redux.js.org/recipes/structuring-reducers/immutable-update-patterns/
   */

  protected _add(newNode: NavigationNode, parent: NavigationNode, tree: NavigationNode) {
    if (tree === parent) {
      console.log(
        `replacing children array of '${parent.name}', adding ${newNode.name}`
      );
      tree.children = [...tree.children!, newNode];
      return true;
    }
    if (!tree.children) {
      console.log(`reached leaf node '${tree.name}', backing out`);
      return false;
    }
    return this.update(tree, this._add.bind(this, newNode, parent));
  }

  _remove(node: NavigationNode, tree: NavigationNode): boolean {
    if (!tree.children) {
      return false;
    }
    const i = tree.children.indexOf(node);
    if (i > -1) {
      tree.children = [
        ...tree.children.slice(0, i),
        ...tree.children.slice(i + 1)
      ];
      console.log(`found ${node.name}, removing it from`, tree);
      return true;
    }
    return this.update(tree, this._remove.bind(this, node));
  }

  protected update(tree: NavigationNode, predicate: (n: NavigationNode) => boolean) {
    let updatedTree: NavigationNode, updatedIndex: number;

    tree.children!.find((node, i) => {
      if (predicate(node)) {
        console.log(`creating new node for '${node.name}'`);
        updatedTree = {...node};
        updatedIndex = i;
        return true;
      }
      return false;
    });

    if (updatedTree!) {
      console.log(`replacing node '${tree.children![updatedIndex!].name}'`);
      tree.children![updatedIndex!] = updatedTree!;
      return true;
    }
    return false;
  }

  public navigateToLogin(): void {
    this.securityService.clearLocalStorage()
    this.reset();
    let node: NavigationNode = this.createNew();
    this.rootNode.children.push(node);
    this.select(node);

    //this.reset();
    //this.router.navigate(['/login'], {queryParams: {returnUrl: this.router.url}});
    //this.router.parseUrl('/login');
  }

  public navigateToHome(): void {
    this.select(this.rootNode);
  }

  public navigateUp(): void {
    this.selectedPath?.pop();
    if (this.selectedPath?.length > 0) {
      this.select(this.selectedPath[this.selectedPath.length - 1]);
    }


  }

  public navigateToUserDetails(): void {
    this.setNavigationTreeByPath(['user-settings', 'user-profile'], this.rootNode)
  }


  public createNew(): NavigationNode {
    return {
      code: "login",
      icon: "login",
      name: "Login",
      routerLink: "login",
      selected: true,
      tooltip: "",
      transient: true,
    }
  }

}
