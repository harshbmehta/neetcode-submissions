class Solution {
    public int maxAreaOfIsland(int[][] grid) {
        int maxArea = 0;
        for(int i=0; i<grid.length;i++) {
            for(int j=0;j<grid[0].length;j++) {
                if(grid[i][j] == 1) {
                    int[] area = {0}; 
                    dfs(grid, i, j, area); 
                    maxArea = Math.max(maxArea, area[0]);
                }
            }
        }
        return maxArea;
    }

    private void dfs(int[][]grid, int i, int j, int[] area) {
        if(i<0 || i>=grid.length || j<0 || j>=grid[0].length || grid[i][j] == 0) {
            return;
        }

        area[0]++;
        grid[i][j] = 0;
        dfs(grid, i+1 ,j, area);
        dfs(grid, i-1,j, area);
        dfs(grid, i ,j+1, area);
        dfs(grid, i ,j-1, area);
    }
}
