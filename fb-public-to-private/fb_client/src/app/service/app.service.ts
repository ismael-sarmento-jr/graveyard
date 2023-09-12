import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthResponse } from './auth-response';
import { PostResponse } from './post-response';

export declare var FB: any;

@Injectable({
  providedIn: 'root'
})
export class AppService {

  authResponse: AuthResponse;

  constructor(
    http: HttpClient
  ) { }

  public login(): Promise<AuthResponse> {
    return new Promise((resolve, reject) => {
        FB.login(response => {
          resolve(response.authResponse);
        }, {scope: 'user_posts'});
    });
  }

  public checkLogin(): Promise<AuthResponse> {
    if(!this.authResponse || !this.authResponse.accessToken) {
      return this.login().then(authResponse => this.authResponse = authResponse);
    }
  }

  public async getPosts(fields: string[]): Promise<PostResponse> {
    await this.checkLogin();
    return new Promise((resolve, reject) => {
      FB.api(
          `/me/posts`,
          'GET',
          {fields},
          response => resolve(response)
      );
    });
  }

  public async changePrivacy(postId: string, privacy: string): Promise<any> {
    await this.checkLogin();
    return new Promise((resolve, reject) => {
      FB.api(
          `/${postId}`,
          'POST',
          {},
          response => resolve(response)
      );
    });
  }
}
