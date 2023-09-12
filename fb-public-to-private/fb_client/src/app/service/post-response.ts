import { Post } from '../model/post';
import { PagingResponse } from './paging-response';

export class PostResponse {
    data: Post[];
    paging: PagingResponse;
}
