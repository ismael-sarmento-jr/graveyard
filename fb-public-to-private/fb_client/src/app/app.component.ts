import { Component, OnInit, AfterViewInit } from '@angular/core';
import { AppService } from './service/app.service';
import { Post } from './model/post';

export declare var FB: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, AfterViewInit{
  title = 'fb-client';
  privacyList: string[] = ['ALL_FRIENDS','SELF','EVERYONE'];
  targetPrivacy: string;
  hiddenPosts: number = 0;
  next: string;

  constructor(
    private service: AppService
  ){}

  ngOnInit() {
    FB.init({
      appId            : '281901936433981',
      autoLogAppEvents : true,
      xfbml            : true,
      version          : 'v7.0'
    });
  }
  ngAfterViewInit() {
    this.service
  }

  fetchAndHidePosts()  {
    this.service.getPosts(['id','privacy','created_time']).then(response => {
      if(response.data){
        this.hidePosts(response.data);
      } else {
        console.log(response);
      }
    });
  }
  
  hidePosts(posts: Post[]) {
    posts.forEach(async post => await this.hidePost(post)
            .then((response) => {
              if(!response.error) {
                this.hiddenPosts++;
              } else {
                console.log(response);
              }
            }));
  }

  hidePost(post: Post) : Promise<any> {
    return new Promise((resolve,reject) => {
      this.service.changePrivacy(post.id, this.targetPrivacy)
            .then((response) => resolve(response))
            .catch((error) => console.log(error));
    });
  }
}
